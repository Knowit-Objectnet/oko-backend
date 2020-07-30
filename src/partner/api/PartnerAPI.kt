package ombruk.backend.partner.api

import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import ombruk.backend.partner.form.PartnerPostForm
import ombruk.backend.partner.form.PartnerUpdateForm
import ombruk.backend.partner.service.IPartnerService
import ombruk.backend.shared.api.*
import ombruk.backend.shared.error.ValidationError

fun Routing.partners(partnerService: IPartnerService) {

    route("/partners") {
        get("/{id}") {
            runCatching { call.parameters["id"]!!.toInt() }
                .fold({ it.right() }, { ValidationError.InputError("Failed to parse id").left() })
                .flatMap { partnerService.getPartnerById(it) }
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }
        }

        get {
            partnerService.getPartners()
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }
        }

        authenticate {
            patch {
                Authorization.authorizeRole(listOf(Roles.RegEmployee), call)
                    .flatMap { receiveCatching { call.receive<PartnerUpdateForm>() } }
                    .flatMap { partnerService.updatePartner(it) }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        authenticate {
            post {
                Authorization.authorizeRole(listOf(Roles.RegEmployee), call)
                    .flatMap { receiveCatching { call.receive<PartnerPostForm>() } }
                    .flatMap { partnerService.savePartner(it) }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        authenticate {
            delete("/{id}") {
                Authorization.authorizeRole(listOf(Roles.RegEmployee), call)
                    .flatMap {
                        catchingCall(ValidationError.InputError("Failed to parse id")) { call.parameters["id"]!!.toInt() }
                    }
                    .flatMap { partnerService.deletePartnerById(it) }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

    }
}