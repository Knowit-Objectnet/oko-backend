package ombruk.backend.aktor.application.api

import arrow.core.extensions.either.monad.flatMap
import arrow.core.extensions.either.monadError.ensure
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.delete
import io.ktor.locations.get
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.patch
import io.ktor.routing.post
import io.ktor.routing.route
import ombruk.backend.aktor.application.api.dto.*
import ombruk.backend.aktor.application.service.IKontaktService
import ombruk.backend.shared.api.Authorization
import ombruk.backend.shared.api.Roles
import ombruk.backend.shared.api.generateResponse
import ombruk.backend.shared.api.receiveCatching
import ombruk.backend.shared.error.AuthorizationError

@KtorExperimentalLocationsAPI
fun Routing.kontakter(kontaktService: IKontaktService) {

    route("/kontakter") {

        get<KontaktGetByIdDto> { form ->
            form.validOrError()
                .flatMap { kontaktService.getKontaktById(it.id) }
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }
        }

        get<KontaktGetDto> { form ->
            form.validOrError()
                .flatMap { kontaktService.getKontakter(it) }
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }
        }

        authenticate {
            patch {
                Authorization.authorizeRole(listOf(Roles.RegEmployee, Roles.Partner, Roles.ReuseStation), call)
                    .map { (role, groupId) ->
                        receiveCatching { call.receive<KontaktUpdateDto>() }
                            .flatMap { it.validOrError() }
                            .map { dto ->
                                kontaktService.getKontaktById(dto.id)
                                .ensure(
                                    { AuthorizationError.AccessViolationError("Denne kontakten tilhører ikke deg")},
                                    {
                                        if (role == Roles.RegEmployee) true
                                        else it.aktorId == groupId
                                    }
                                )
                                .flatMap { kontaktService.update(dto) }
                            }.flatMap { it }
                    }.flatMap { it }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        authenticate {
            post {
                Authorization.authorizeRole(listOf(Roles.RegEmployee, Roles.Partner, Roles.ReuseStation), call)
                    .map { (role, groupId) ->
                        receiveCatching { call.receive<KontaktSaveDto>() }
                            .flatMap { it.validOrError() }
                            .ensure(
                                {AuthorizationError.AccessViolationError("Kontakt forsøkt opprettet for annen gruppe")},
                                {
                                    if (role == Roles.RegEmployee) true
                                    else it.aktorId == groupId
                                }
                            )
                            .flatMap { kontaktService.save(it) }
                    }.flatMap { it }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        authenticate {
            delete<KontaktDeleteDto> { form ->
                Authorization.authorizeRole(listOf(Roles.RegEmployee, Roles.Partner, Roles.ReuseStation), call)
                    .map { (role, groupId) ->
                        form.validOrError()
                            .map { dto ->
                                kontaktService.getKontaktById(dto.id)
                                    .ensure(
                                        { AuthorizationError.AccessViolationError("Denne kontakten tilhører ikke deg")},
                                        {
                                            if (role == Roles.RegEmployee) true
                                            else it.aktorId == groupId
                                        }
                                    )
                                    .flatMap { kontaktService.deleteKontaktById(it.id) }
                            }.flatMap { it }
                    }.flatMap { it }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }
    }
}