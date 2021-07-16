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
import java.util.*

@KtorExperimentalLocationsAPI
fun Routing.kontakter(kontaktService: IKontaktService) {

    route("/kontakter") {

        authenticate {
            get<KontaktGetByIdDto> { form ->
                Authorization.authorizeRole(listOf(Roles.RegEmployee, Roles.Partner, Roles.ReuseStation), call)
                    .flatMap { (role, groupId) ->
                        form.validOrError()
                            .flatMap { kontaktService.getKontaktById(it.id) }
                            .ensure(
                                { AuthorizationError.AccessViolationError("Denne kontakten tilhører ikke deg")},
                                {
                                    if (role == Roles.RegEmployee) true
                                    else it.aktorId == groupId
                                }
                            )
                    }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        authenticate {
            get<KontaktGetDto> { form ->
                Authorization.authorizeRole(listOf(Roles.RegEmployee, Roles.Partner, Roles.ReuseStation), call)
                    .flatMap { (role, groupId) ->
                        form.validOrError()
                            .map { if (role == Roles.RegEmployee) it else it.copy(aktorId = groupId) }
                            .flatMap { kontaktService.getKontakter(it) }
                    }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        authenticate {
            patch {
                Authorization.authorizeRole(listOf(Roles.RegEmployee, Roles.Partner, Roles.ReuseStation), call)
                    .flatMap { (role, groupId) ->
                        receiveCatching { call.receive<KontaktUpdateDto>() }
                            .flatMap { it.validOrError() }
                            .flatMap { dto ->
                                kontaktService.getKontaktById(dto.id)
                                .ensure(
                                    { AuthorizationError.AccessViolationError("Denne kontakten tilhører ikke deg")},
                                    {
                                        if (role == Roles.RegEmployee) true
                                        else it.aktorId == groupId
                                    }
                                )
                                .flatMap { kontaktService.update(dto) }
                            }
                    }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        authenticate {
            post {
                Authorization.authorizeRole(listOf(Roles.RegEmployee, Roles.Partner, Roles.ReuseStation), call)
                    .flatMap { (role, groupId) ->
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
                    }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        authenticate {
            post("/verifiser") {
                Authorization.authorizeRole(listOf(Roles.RegEmployee, Roles.Partner, Roles.ReuseStation), call)
                    .flatMap { (role, groupId) ->
                        receiveCatching { call.receive<KontaktVerifiseringDto>() }
                            .flatMap { it.validOrError() }
                            .ensure(
                                {AuthorizationError.AccessViolationError("Kontakt forsøkt verifisert for annen gruppe")},
                                {
                                    if (role == Roles.RegEmployee) true
                                    else it.id == groupId
                                }
                            )
                            .flatMap { kontaktService.verifiserKontakt(it) }
                    }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        authenticate {
            post("/verifisering-resend/{kontaktId}") {
                Authorization.authorizeRole(listOf(Roles.RegEmployee, Roles.Partner, Roles.ReuseStation), call)
                    .flatMap { (role, groupId) ->

                        kontaktService.getKontaktById(UUID.fromString(call.parameters["kontaktId"]))
                            .ensure(
                                {AuthorizationError.AccessViolationError("Kontakt forsøkt verifisert for annen gruppe")},
                                {
                                    if (role == Roles.RegEmployee) true
                                    else it.id == groupId
                                }
                            )
                            .flatMap { kontaktService.resendVerifikasjon(it) }
                    }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        authenticate {
            delete<KontaktDeleteDto> { form ->
                Authorization.authorizeRole(listOf(Roles.RegEmployee, Roles.Partner, Roles.ReuseStation), call)
                    .flatMap { (role, groupId) ->
                        form.validOrError()
                            .flatMap { dto ->
                                kontaktService.getKontaktById(dto.id)
                                    .ensure(
                                        { AuthorizationError.AccessViolationError("Denne kontakten tilhører ikke deg")},
                                        {
                                            if (role == Roles.RegEmployee) true
                                            else it.aktorId == groupId
                                        }
                                    )
                                    .flatMap { kontaktService.deleteKontaktById(it.id) }
                            }
                    }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }
    }
}