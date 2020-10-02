package no.oslokommune.ombruk.stasjon.api

import arrow.core.flatMap
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
import no.oslokommune.ombruk.stasjon.form.*
import no.oslokommune.ombruk.stasjon.service.IStasjonService
import no.oslokommune.ombruk.shared.api.Authorization
import no.oslokommune.ombruk.shared.api.Roles
import no.oslokommune.ombruk.shared.api.generateResponse
import no.oslokommune.ombruk.shared.api.receiveCatching

@KtorExperimentalLocationsAPI
fun Routing.stasjoner(stasjonService: IStasjonService) {

    route("/stasjoner") {

        get<StasjonGetByIdForm> { form ->
            form.validOrError()
                .flatMap { stasjonService.getStasjonById(it.id) }
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }
        }

        get<StasjonGetForm> { form ->
            form.validOrError()
                .flatMap { stasjonService.getStasjoner(it) }
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }
        }

        authenticate {
            post {
                Authorization.authorizeRole(listOf(Roles.RegEmployee), call)
                    .flatMap { receiveCatching { call.receive<StasjonPostForm>() } }
                    .flatMap { it.validOrError() }
                    .flatMap { stasjonService.saveStasjon(it) }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        authenticate {
            patch {
                Authorization.authorizeRole(listOf(Roles.RegEmployee), call)
                    .flatMap { receiveCatching { call.receive<StasjonUpdateForm>() } }
                    .flatMap { it.validOrError() }
                    .flatMap { stasjonService.updateStasjon(it) }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        authenticate {
            delete<StasjonDeleteForm> { form ->

                Authorization.authorizeRole(listOf(Roles.RegEmployee), call)
                    .flatMap { form.validOrError() }
                    .flatMap { stasjonService.deleteStasjonById(it.id) }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }
    }
}