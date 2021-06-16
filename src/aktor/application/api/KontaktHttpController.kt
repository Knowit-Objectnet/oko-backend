package ombruk.backend.aktor.application.api

import arrow.core.extensions.either.monad.flatMap
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

        //TODO: Correct station and partner are authorized for updating this kontakt
        authenticate {
            patch {
                Authorization.authorizeRole(listOf(Roles.RegEmployee, Roles.Partner, Roles.ReuseStation), call)
                    .flatMap { receiveCatching { call.receive<KontaktUpdateDto>() } }
                    .flatMap { it.validOrError() }
                    .flatMap { kontaktService.update(it) }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        //TODO: Correct station and partner are authorized for saving this kontakt to a station or partner
        authenticate {
            post {
                Authorization.authorizeRole(listOf(Roles.RegEmployee, Roles.Partner, Roles.ReuseStation), call)
                    .flatMap { receiveCatching { call.receive<KontaktSaveDto>() } }
                    .flatMap { it.validOrError() }
                    .flatMap { kontaktService.save(it) }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        //TODO: Correct station and partner are authorized for deleting this kontakt
        authenticate {
            delete<KontaktDeleteDto> { form ->
                Authorization.authorizeRole(listOf(Roles.RegEmployee, Roles.Partner, Roles.ReuseStation), call)
                    .flatMap { form.validOrError() }
                    .flatMap { kontaktService.deleteKontaktById(it.id) }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }
    }
}