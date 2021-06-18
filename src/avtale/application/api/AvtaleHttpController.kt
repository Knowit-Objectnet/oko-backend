package ombruk.backend.avtale.application.api.dto

import arrow.core.extensions.either.monad.flatMap
import arrow.core.extensions.either.monadError.ensure
import arrow.core.flatMap
import avtale.application.api.dto.AvtaleSaveDto
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import ombruk.backend.aktor.application.api.dto.KontaktUpdateDto
import ombruk.backend.avtale.application.service.IAvtaleService
import ombruk.backend.shared.api.Authorization
import ombruk.backend.shared.api.Roles
import ombruk.backend.shared.api.generateResponse
import ombruk.backend.shared.api.receiveCatching
import ombruk.backend.shared.error.AuthorizationError

@KtorExperimentalLocationsAPI
fun Routing.avtaler(avtaleService: IAvtaleService) {

    route("/avtaler") {

        authenticate {
            get<AvtaleFindOneDto> { form ->
                Authorization.authorizeRole(listOf(Roles.RegEmployee, Roles.Partner, Roles.ReuseStation), call)
                    .flatMap { (role, groupId) ->
                        form.validOrError()
                            .flatMap { avtaleService.findOne(form.id) }
                            .ensure(
                                { AuthorizationError.AccessViolationError("Avtalen tilhører ikke deg")},
                                {if (role != Roles.RegEmployee) it.aktorId == groupId else true}
                            )
                    }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        authenticate {
            get<AvtaleFindDto> { form ->
                Authorization.authorizeRole(listOf(Roles.RegEmployee, Roles.Partner, Roles.ReuseStation), call)
                    .flatMap { (role, groupId) ->
                        form.validOrError()
                            .map { if(role == Roles.RegEmployee) it else it.copy(aktorId = groupId) }
                            .flatMap { avtaleService.find(it) }
                    }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        authenticate {
            post {
                Authorization.authorizeRole(listOf(Roles.RegEmployee), call)
                    .flatMap { receiveCatching { call.receive<AvtaleSaveDto>() } }
                    .flatMap { it.validOrError() }
                    .flatMap { avtaleService.save(it) }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        authenticate {
            delete<AvtaleDeleteDto> { form ->
                Authorization.authorizeRole(listOf(Roles.RegEmployee), call)
                    .flatMap { form.validOrError() }
                    .flatMap { avtaleService.archiveOne(form.id) }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        authenticate {
            patch {
                Authorization.authorizeRole(listOf(Roles.RegEmployee), call)
                    .flatMap { receiveCatching { call.receive<AvtaleUpdateDto>() } }
                    .flatMap { it.validOrError() }
                    .flatMap { avtaleService.update(it) }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }
    }
}