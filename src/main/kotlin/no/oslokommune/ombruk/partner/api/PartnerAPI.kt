package no.oslokommune.ombruk.partner.api

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
import no.oslokommune.ombruk.partner.form.*
import no.oslokommune.ombruk.partner.service.IPartnerService
import no.oslokommune.ombruk.shared.api.Authorization
import no.oslokommune.ombruk.shared.api.Roles
import no.oslokommune.ombruk.shared.api.generateResponse
import no.oslokommune.ombruk.shared.api.receiveCatching

@KtorExperimentalLocationsAPI
fun Routing.partnere(partnerService: IPartnerService) {

    route("/partnere") {
        get<PartnerGetByIdForm> { form ->
            form.validOrError()
                .flatMap { partnerService.getPartnerById(it.id) }
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }
        }

        get<PartnerGetForm> { form ->
            form.validOrError()
                .flatMap { partnerService.getPartnere(it) }
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }
        }

        authenticate {
            patch {
                Authorization.authorizeRole(listOf(Roles.RegEmployee), call)
                    .flatMap { receiveCatching { call.receive<PartnerUpdateForm>() } }
                    .flatMap { it.validOrError() }
                    .flatMap { partnerService.updatePartner(it) }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        authenticate {
            post {
                Authorization.authorizeRole(listOf(Roles.RegEmployee), call)
                    .flatMap { receiveCatching { call.receive<PartnerPostForm>() } }
                    .flatMap { it.validOrError() }
                    .flatMap { partnerService.savePartner(it) }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        authenticate {
            delete<PartnerDeleteForm> { form ->
                Authorization.authorizeRole(listOf(Roles.RegEmployee), call)
                    .flatMap { form.validOrError() }
                    .flatMap { partnerService.deletePartnerById(it.id) }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

    }
}