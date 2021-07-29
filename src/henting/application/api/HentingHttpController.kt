package ombruk.backend.henting.application.api.dto


import arrow.core.extensions.either.monad.flatMap
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import ombruk.backend.henting.application.service.IHentingService
import ombruk.backend.shared.api.Authorization
import ombruk.backend.shared.api.Roles
import ombruk.backend.shared.api.generateResponse


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