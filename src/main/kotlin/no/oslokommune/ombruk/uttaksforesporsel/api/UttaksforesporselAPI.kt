package no.oslokommune.ombruk.uttaksforesporsel.api

import arrow.core.flatMap
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.delete
import io.ktor.locations.get
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.post
import io.ktor.routing.route
import no.oslokommune.ombruk.uttaksforesporsel.form.uttaksforesporsel.UttaksforesporselDeleteForm
import no.oslokommune.ombruk.uttaksforesporsel.form.uttaksforesporsel.UttaksForesporselGetForm
import no.oslokommune.ombruk.uttaksforesporsel.form.uttaksforesporsel.UttaksforesporselPostForm
import no.oslokommune.ombruk.uttaksforesporsel.service.IUttaksforesporselService
import no.oslokommune.ombruk.shared.api.Authorization
import no.oslokommune.ombruk.shared.api.Roles
import no.oslokommune.ombruk.shared.api.generateResponse
import no.oslokommune.ombruk.shared.api.receiveCatching


@KtorExperimentalLocationsAPI
fun Routing.request(uttaksforesporselService: IUttaksforesporselService) {

    route("/uttaksforesporsel") {

        authenticate {
            post {
                receiveCatching { call.receive<UttaksforesporselPostForm>() }.flatMap { form ->
                    Authorization.authorizeRole(listOf(Roles.Partner), call)
                        .flatMap { Authorization.authorizeRequestId(it, form.partnerId) }
                        .flatMap { form.validOrError() }
                        .flatMap { uttaksforesporselService.saveRequest(it) }
                }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        get<UttaksForesporselGetForm> { form ->
            form.validOrError()
                .flatMap { uttaksforesporselService.getRequests(form) }
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }
        }

        authenticate {
            delete<UttaksforesporselDeleteForm> { form ->
                Authorization.authorizeRole(listOf(Roles.Partner), call)
                    .flatMap { Authorization.authorizeRequestId(it, form.partnerId) }
                    .flatMap { form.validOrError() }
                    .flatMap { uttaksforesporselService.deleteRequest(form) }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }
    }
}