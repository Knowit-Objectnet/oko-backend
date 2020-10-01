package no.oslokommune.ombruk.station.api

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
import no.oslokommune.ombruk.station.form.*
import no.oslokommune.ombruk.station.service.IStationService
import no.oslokommune.ombruk.shared.api.Authorization
import no.oslokommune.ombruk.shared.api.Roles
import no.oslokommune.ombruk.shared.api.generateResponse
import no.oslokommune.ombruk.shared.api.receiveCatching

@KtorExperimentalLocationsAPI
fun Routing.stations(stationService: IStationService) {

    route("/stations") {

        get<StationGetByIdForm> { form ->
            form.validOrError()
                .flatMap { stationService.getStationById(it.id) }
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }
        }

        get<StationGetForm> { form ->
            form.validOrError()
                .flatMap { stationService.getStations(it) }
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }
        }

        authenticate {
            post {
                Authorization.authorizeRole(listOf(Roles.RegEmployee), call)
                    .flatMap { receiveCatching { call.receive<StationPostForm>() } }
                    .flatMap { it.validOrError() }
                    .flatMap { stationService.saveStation(it) }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        authenticate {
            patch {
                Authorization.authorizeRole(listOf(Roles.RegEmployee), call)
                    .flatMap { receiveCatching { call.receive<StationUpdateForm>() } }
                    .flatMap { it.validOrError() }
                    .flatMap { stationService.updateStation(it) }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        authenticate {
            delete<StationDeleteForm> { form ->

                Authorization.authorizeRole(listOf(Roles.RegEmployee), call)
                    .flatMap { form.validOrError() }
                    .flatMap { stationService.deleteStationById(it.id) }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }
    }
}