package ombruk.backend.pickup.api

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.delete
import io.ktor.routing.get
import io.ktor.routing.post
import ombruk.backend.pickup.model.Request
import ombruk.backend.pickup.service.IRequestService


fun Routing.request(requestService: IRequestService) {

    post("/requests/") {
        val body = runCatching { call.receive<Request>() }.onFailure {
            call.respond(HttpStatusCode.InternalServerError, "Failed to add request") }.getOrThrow()
        val result = requestService.addPartnersToPickup(body)
        call.respond(HttpStatusCode.Created, result)
    }

    get("/requests/") {
        val params = call.request.queryParameters
        try {
            call.respond(requestService.getRequests(params["pickupID"]?.toInt(), params["partnerID"]?.toInt()))
        } catch (e: NumberFormatException) {
            e.printStackTrace()
            call.respond(HttpStatusCode.BadRequest)
        }
    }

    delete("/requests/") {
        val params = call.request.queryParameters
        try {
            when(requestService.deleteRequests(params["pickupID"]?.toInt(), params["partnerID"]?.toInt(), params["stationID"]?.toInt())) {
                true -> call.respond(HttpStatusCode.OK)
                else -> call.respond(HttpStatusCode.NotFound)
            }
        } catch (e: NumberFormatException) {
            e.printStackTrace()
            call.respond(HttpStatusCode.BadRequest)
        }
    }
}