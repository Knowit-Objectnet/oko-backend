package no.oslokommune.ombruk.uttak.api

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
import no.oslokommune.ombruk.uttak.form.*
import no.oslokommune.ombruk.uttak.service.IUttakService
import no.oslokommune.ombruk.shared.api.generateResponse
import no.oslokommune.ombruk.shared.api.receiveCatching
import no.oslokommune.ombruk.shared.api.Authorization
import no.oslokommune.ombruk.shared.api.Roles

@KtorExperimentalLocationsAPI
fun Routing.uttak(uttakService: IUttakService) {
    route("/uttak") {
        get<UttakGetByIdForm> { form ->
            form.validOrError()
                .flatMap { uttakService.getUttakByID(it.id) }
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }
        }

        get<UttakGetForm> { form ->
            form.validOrError()
                .flatMap { uttakService.getUttak(it) }
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }
        }

        authenticate {
            post("/") {
                receiveCatching { call.receive<UttakPostForm>() }.flatMap { form ->
                    Authorization.authorizeRole(listOf(Roles.RegEmployee), call)
                        .flatMap { form.validOrError() }
                        .flatMap { uttakService.saveUttak(it) }
                }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        authenticate {
            patch("/") {
                receiveCatching { call.receive<UttakUpdateForm>() }.flatMap { form ->
                    Authorization.authorizeRole(listOf(Roles.ReuseStasjon, Roles.RegEmployee), call)
                        .flatMap { form.validOrError() }
                        .flatMap { uttakService.updateUttak(it) }
                }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        authenticate {
            delete<UttakDeleteForm> { form ->
                Authorization.authorizeRole(listOf(Roles.RegEmployee, Roles.ReuseStasjon, Roles.Partner), call)
                    .map { if (it.first == Roles.Partner) form.partnerId = it.second; it }
                    .flatMap { Authorization.authorizePartnerID(it) { uttakService.getUttak(form.toGetForm()) } }
                    .flatMap { form.validOrError() }
                    .flatMap { uttakService.deleteUttak(it) }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }
    }
}


@KtorExperimentalLocationsAPI
private fun UttakDeleteForm.toGetForm() =
    UttakGetForm(
        id,
        gjentakelsesRegelID = gjentakelsesRegelId,
        startTidspunkt = fromDate,
        sluttTidspunkt = toDate,
        stasjonID = stasjonId,
        partnerID = partnerId
    )