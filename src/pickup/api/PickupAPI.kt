package ombruk.backend.pickup.api

import arrow.core.flatMap
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.get
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import kotlinx.coroutines.runBlocking
import ombruk.backend.pickup.form.CreatePickupForm
import ombruk.backend.pickup.form.GetPickupsForm
import ombruk.backend.pickup.form.PatchPickupForm
import ombruk.backend.pickup.model.Pickup
import ombruk.backend.pickup.service.IPickupService
import ombruk.backend.shared.api.generateResponse
import ombruk.backend.shared.api.receiveCatching

fun Routing.pickup(pickupService: IPickupService) {

    route("/pickups") {

        delete("/") {
            val params = call.request.queryParameters
            try {
                when (pickupService.deletePickup(params["pickupID"]?.toInt(), params["stationID"]?.toInt())) {
                    true -> call.respond(HttpStatusCode.OK)
                    else -> call.respond(HttpStatusCode.NotFound)
                }
            } catch (e: NumberFormatException) {
                e.printStackTrace()
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        patch("/") {
            receiveCatching { call.receive<PatchPickupForm>() }
                .flatMap { pickupService.updatePickup(it) }
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }
        }

        post("/") {
            receiveCatching { call.receive<CreatePickupForm>() }
                .flatMap { pickupService.savePickup(it) }
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }
        }
        // Location is set in the form.
        get<GetPickupsForm> { form ->
            pickupService.getPickups(form)
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }

        }

        get("/{pickup_id}") {
            val id = call.parameters["pickup_id"]?.toInt() ?: throw IllegalArgumentException("Invalid pickup id")
            when (val pickup = pickupService.getPickupById(id)) {
                null -> call.respond(HttpStatusCode.NotFound, "Pickup id not found")
                else -> call.respond(HttpStatusCode.OK, pickup)
            }
        }
    }
}