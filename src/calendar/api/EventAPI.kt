package ombruk.backend.calendar.api

import ombruk.backend.shared.api.Authorization
import ombruk.backend.shared.api.Roles
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import kotlinx.serialization.json.JsonDecodingException
import ombruk.backend.shared.error.RequestError
import ombruk.backend.shared.api.generateResponse
import ombruk.backend.calendar.form.EventUpdateForm
import ombruk.backend.calendar.form.CreateEventForm
import ombruk.backend.calendar.form.EventDeleteForm
import ombruk.backend.calendar.model.validator.EventUpdateFormValidator
import ombruk.backend.calendar.model.validator.EventValidatorCode
import ombruk.backend.calendar.service.IEventService
import ombruk.calendar.form.api.EventGetForm
import java.time.format.DateTimeParseException


fun Routing.events(eventService: IEventService) {

    get("/events/{event_id}") {
        runCatching { call.parameters["event_id"]?.toInt()!! }
            .fold({ it.right() }, { RequestError.InvalidIdError().left() })
            .flatMap { eventService.getEventByID(it) }
            .run { generateResponse(this) }
            .also { (code, response) -> call.respond(code, response) }
    }

    get("/events/") {
        EventGetForm.create(call.request.queryParameters)
            .flatMap { eventService.getEvents(it) }
            .run { generateResponse(this) }
            .also { (code, response) -> call.respond(code, response) }
    }

    authenticate {
        post("/events/") {
            val event = runCatching { call.receive<CreateEventForm>() }.onFailure {
                if (it.message == null) return@onFailure
                when (it) {
                    is JsonDecodingException -> call.respond(HttpStatusCode.BadRequest, it.message!!)
                    is DateTimeParseException -> call.respond(HttpStatusCode.BadRequest, it.message!!)
                }
            }.getOrThrow()

            Authorization.authorizeRole(listOf(Roles.RegEmployee), call)
                .flatMap { eventService.saveEvent(event) }
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }
        }
    }

    authenticate {
        patch("/events/") {
            val event = kotlin.runCatching { call.receive<EventUpdateForm>() }.onFailure {
                call.respond(HttpStatusCode.BadRequest)
            }.getOrThrow()
            val code = EventUpdateFormValidator.validate(event)
            if (code != EventValidatorCode.OK) {
                call.respond(HttpStatusCode.BadRequest, code.info!!)
                return@patch
            }
            Authorization.authorizeRole(listOf(Roles.ReuseStation, Roles.RegEmployee), call)
                .map { eventService.updateEvent(event) }
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }
        }
    }

    fun delete(auth: Pair<Roles, Int>, call: ApplicationCall) =
        EventDeleteForm.create(call.request.queryParameters)
            .flatMap { deleteForm ->
                EventGetForm.create(call.request.queryParameters)
                    .flatMap { Authorization.authorizePartnerID(auth) { eventService.getEvents(it) } }
                    .fold({ it.left() }, { eventService.deleteEvent(deleteForm) })
            }

    authenticate {
        delete("/events/") {
            Authorization.authorizeRole(listOf(Roles.RegEmployee, Roles.ReuseStation, Roles.Partner), call)
                .flatMap { delete(it, call) }
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }
        }
    }
}