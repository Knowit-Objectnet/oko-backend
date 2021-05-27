package ombruk.backend.aktor.application.api

import arrow.core.flatMap
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.delete
import io.ktor.locations.get
import io.ktor.request.receive
import io.ktor.response.*
import io.ktor.routing.*
import ombruk.backend.aktor.application.api.dto.*
import ombruk.backend.aktor.application.service.IStasjonService
import ombruk.backend.shared.api.Authorization
import ombruk.backend.shared.api.Roles
import ombruk.backend.shared.api.generateResponse
import ombruk.backend.shared.api.receiveCatching
import java.util.*

@KtorExperimentalLocationsAPI
fun Routing.stasjoner(stasjonService: IStasjonService) {


    route("/stasjoner") {
        get<StasjonFindOneDto> { form ->
            form.validOrError()
                .flatMap { stasjonService.findOne(it.id) }
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }
        }

        get<StasjonFindDto> { form ->
            form.validOrError()
                .flatMap { stasjonService.find(it) }
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }
        }

        authenticate {
            post {
                Authorization.authorizeRole(listOf(Roles.RegEmployee), call)
                    .flatMap { receiveCatching { call.receive<StasjonCreateDto>() } }
                    .flatMap { it.validOrError() }
                    .flatMap { stasjonService.save(it) }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        authenticate {
            patch {
                Authorization.authorizeRole(listOf(Roles.RegEmployee), call)
                    .flatMap { receiveCatching { call.receive<StasjonUpdateDto>() } }
                    .flatMap { it.validOrError() }
                    .flatMap { stasjonService.update(it) }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        authenticate {
            delete<StasjonDeleteDto> { form ->

                Authorization.authorizeRole(listOf(Roles.RegEmployee), call)
                    .flatMap { form.validOrError() }
                    .flatMap { stasjonService.delete(it.id) }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }
    }
}