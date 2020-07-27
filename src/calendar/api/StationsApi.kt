package ombruk.backend.calendar.api

import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import kotlinx.coroutines.runBlocking
import ombruk.backend.calendar.form.StationPostForm
import ombruk.backend.calendar.form.StationUpdateForm
import ombruk.backend.calendar.service.IStationService
import ombruk.backend.shared.api.*
import ombruk.backend.shared.error.RequestError
import org.slf4j.LoggerFactory

fun Routing.stations(stationService: IStationService) {

    route("/stations") {

        get("/{id}") {
            runCatching { call.parameters["id"]!!.toInt() }
                .fold({ it.right() }, { RequestError.InvalidIdError(call.parameters["id"] ?: "").left() })
                .flatMap { stationService.getStationById(it) }
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }
        }

        get {
            stationService.getStations()
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }
        }

        authenticate {
            post {
                Authorization.authorizeRole(listOf(Roles.RegEmployee), call)
                    .flatMap { catchingCall(RequestError.MangledRequestBody()) { runBlocking { call.receive<StationPostForm>() } } }
                    .flatMap { stationService.saveStation(it) }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        authenticate {
            patch {
                Authorization.authorizeRole(listOf(Roles.RegEmployee), call)
                    .flatMap { catchingCall(RequestError.MangledRequestBody()) { runBlocking { call.receive<StationUpdateForm>() } } }
                    .flatMap { stationService.updateStation(it) }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        authenticate {
            delete("/{id}") {
                Authorization.authorizeRole(listOf(Roles.RegEmployee), call)
                    .flatMap {
                        catchingCall(RequestError.InvalidIdError(call.parameters["id"] ?: ""))
                        { runBlocking { call.parameters["id"]!!.toInt() } }
                    }
                    .flatMap { stationService.deleteStationById(it) }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }
    }
}