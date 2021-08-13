package ombruk.backend.henting.application.api.dto


import arrow.core.extensions.either.monad.flatMap
import arrow.core.extensions.either.monadError.ensure
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import ombruk.backend.henting.application.service.IHentingService
import ombruk.backend.shared.api.Authorization
import ombruk.backend.shared.api.Roles
import ombruk.backend.shared.api.generateResponse
import ombruk.backend.shared.error.AuthorizationError


@KtorExperimentalLocationsAPI
fun Routing.hentinger(hentingService: IHentingService) {

    route("/hentinger") {

        authenticate {
            get<HentingFindOneDto> { form ->
                Authorization.authorizeRole(listOf(Roles.RegEmployee, Roles.ReuseStation, Roles.Partner), call)
                    .flatMap { (role, groupId) ->
                        form.validOrError()
                            .flatMap {
                                if (role == Roles.Partner) hentingService.findOne(form.id, groupId)
                                else hentingService.findOne(form.id)
                            }
                            .ensure(
                                {AuthorizationError.AccessViolationError("Ekstrahenting ikke utlyst til deg")},
                                {when (role) {
                                    Roles.Partner -> it.aktorId == groupId || (it.ekstraHenting != null && it.ekstraHenting.utlysninger.size == 1)
                                    Roles.ReuseStation -> it.stasjonId == groupId
                                    Roles.RegEmployee -> true
                                }}
                            )
                    }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }
        authenticate {
            get<HentingFindDto> { form ->
                Authorization.authorizeRole(listOf(Roles.RegEmployee, Roles.ReuseStation, Roles.Partner), call)
                    .flatMap { (role, groupId) ->
                        form.validOrError()
                            .map { if (role == Roles.ReuseStation) it.copy(stasjonId = groupId) else it}
                            .flatMap {
                                if (role == Roles.Partner) hentingService.find(it, groupId)
                                else hentingService.find(it)
                            }
                    }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }
    }
}