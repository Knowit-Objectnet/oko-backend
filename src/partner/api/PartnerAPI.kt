package ombruk.backend.partner.api

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
import ombruk.backend.partner.form.*
import ombruk.backend.partner.service.IPartnerService
import ombruk.backend.shared.api.Authorization
import ombruk.backend.shared.api.Roles
import ombruk.backend.shared.api.generateResponse
import ombruk.backend.shared.api.receiveCatching

@KtorExperimentalLocationsAPI
fun Routing.partners(partnerService: IPartnerService) {

    route("/partners") {
        get<PartnerGetByIdForm> { form ->
            form.validOrError()
                .flatMap { partnerService.getPartnerById(it.id) }
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }
        }

        get<PartnerGetForm> { form ->
            form.validOrError()
                .flatMap { partnerService.getPartners(it) }
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