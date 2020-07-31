package ombruk.backend.pickup.api

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.get
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import ombruk.backend.pickup.form.CreatePickupForm
import ombruk.backend.pickup.form.GetPickupsForm
import ombruk.backend.pickup.model.Pickup
import ombruk.backend.pickup.service.IPickupService

fun Routing.pickup(pickupService: IPickupService) {
    get("/") {
        call.respond("Ombruk er kult")
    }

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
            val body = runCatching { call.receive<Pickup>() }.onFailure {
                call.respond(HttpStatusCode.InternalServerError, "Failed to update pickup")
            }.getOrThrow()
            try {
                when (pickupService.updatePickup(body)) {
                    true -> call.respond(HttpStatusCode.OK)
                    else -> call.respond(HttpStatusCode.NotFound)
                }
            } catch (e: NumberFormatException) {
                e.printStackTrace()
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        post("/") {
            val form = runCatching { call.receive<CreatePickupForm>() }.onFailure {
                call.respond(HttpStatusCode.InternalServerError, "Failed to create pickup")
            }.getOrThrow()
            val result = pickupService.savePickup(form)
            call.respond(HttpStatusCode.Created, result)
        }
        // Location is set in the form.
        get<GetPickupsForm> { form ->
            try {
                call.respond(pickupService.getPickups(form))
            } catch (e: NumberFormatException) {
                e.printStackTrace()
                call.respond(HttpStatusCode.BadRequest)
            }
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