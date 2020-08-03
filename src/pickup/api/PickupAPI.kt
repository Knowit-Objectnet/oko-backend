package ombruk.backend.pickup.api

import arrow.core.flatMap
import io.ktor.application.call
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
import io.ktor.locations.delete
import io.ktor.locations.get
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import ombruk.backend.pickup.form.pickup.*
import ombruk.backend.pickup.service.IPickupService
import ombruk.backend.shared.api.Authorization
import ombruk.backend.shared.api.Roles
import ombruk.backend.shared.api.generateResponse
import ombruk.backend.shared.api.receiveCatching

fun Routing.pickup(pickupService: IPickupService) {

    route("/pickups") {

        get<PickupGetForm> { form ->
            form.validOrError()
                .flatMap { pickupService.getPickups(form) }
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }
        }

        get<PickupGetByIdForm> { form ->
            form.validOrError()
                .flatMap { pickupService.getPickupById(form) }
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }
        }

        authenticate {
            post {
                Authorization.authorizeRole(listOf(Roles.ReuseStation, Roles.RegEmployee), call)
                    .flatMap { receiveCatching { call.receive<PickupPostForm>() } }
                    .flatMap { it.validOrError() }
                    .flatMap { (pickupService.savePickup(it)) }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }


            authenticate {
                patch {
                    Authorization.authorizeRole(listOf(Roles.ReuseStation, Roles.RegEmployee), call)
                        .flatMap { receiveCatching { call.receive<PickupUpdateForm>() } }
                        .flatMap { it.validOrError() }
                        .flatMap { pickupService.updatePickup(it) }
                        .run { generateResponse(this) }
                        .also { (code, response) -> call.respond(code, response) }
                }
            }


            authenticate {
                delete<PickupDeleteForm> { form ->
                    Authorization.authorizeRole(listOf(Roles.ReuseStation, Roles.RegEmployee), call)
                        .flatMap { form.validOrError() }
                        .flatMap { pickupService.deletePickup(form) }
                        .run { generateResponse(this) }
                        .also { (code, response) -> call.respond(code, response) }
                }
            }
        }
    }
}