package ombruk.backend.calendar.api

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import kotlinx.serialization.json.JsonDecodingException
import ombruk.backend.calendar.form.StationForm
import ombruk.backend.calendar.model.Station
import ombruk.backend.calendar.service.StationService
import org.slf4j.LoggerFactory
import java.time.format.DateTimeParseException

private val logger = LoggerFactory.getLogger("ombruk.backend.api.StationsApi")

fun Routing.stations() {

    route("/stations") {
        get("/{id}") {
            val id = kotlin.runCatching { call.parameters["id"]!!.toInt() }
                .getOrElse { call.respond(HttpStatusCode.BadRequest, "Invalid id in URL"); return@get }

            StationService.getStationById(id).fold(
                { call.respond(HttpStatusCode.InternalServerError, "Failed to get station") },
                {
                    if (it != null) call.respond(HttpStatusCode.OK, it)
                    else call.respond(HttpStatusCode.NotFound)
                })
        }

        get {
            StationService.getStations().fold(
                { call.respond(HttpStatusCode.InternalServerError, "Failed to get station") },
                { call.respond(HttpStatusCode.OK, it) })
        }

        post {
            val station = runCatching { call.receive<StationForm>() }
                .map { Station(0, it.name) }
                .getOrElse {
                    it.printStackTrace()
                    logger.warn("Failed to parse Station: ${it.message}")
                    call.respond(HttpStatusCode.BadRequest,
                        parseErrorDecoder(it)
                    )
                    return@post
                }

            StationService.saveStation(station).fold(
                { call.respond(HttpStatusCode.InternalServerError, "Failed to save station") },
                { call.respond(HttpStatusCode.Created, it) })
        }

        patch {
            val station = runCatching { call.receive<Station>() }
                .getOrElse {
                    logger.warn("Failed to parse Station: ${it.message}")
                    call.respond(HttpStatusCode.BadRequest,
                        parseErrorDecoder(it)
                    )
                    return@patch
                }

            StationService.updateStation(station).fold(
                { call.respond(HttpStatusCode.InternalServerError, "Failed to update station") },
                { call.respond(HttpStatusCode.OK, it) })
        }

        delete("/{id}") {
            val id = kotlin.runCatching { call.parameters["id"]!!.toInt() }
                .getOrElse { call.respond(HttpStatusCode.BadRequest, "Invalid id in URL"); return@delete }

            StationService.deleteStationById(id).fold(
                { call.respond(HttpStatusCode.InternalServerError, "Failed to get station") },
                { call.respond(HttpStatusCode.OK, it) })
        }
    }

}

private fun parseErrorDecoder(error: Throwable): String {
    logger.warn("Failed to parse request message: ${error.message}")
    if (error.message == null) return "Failed to parse body"
    return when (error) {
        is JsonDecodingException, is DateTimeParseException -> error.message!!
        else -> "Failed to parse body"
    }
}