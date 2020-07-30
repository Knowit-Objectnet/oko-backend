package ombruk.backend.calendar.api

import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import calendar.form.EventGetForm
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.delete
import io.ktor.locations.get
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.patch
import io.ktor.routing.post
import ombruk.backend.calendar.form.EventDeleteForm
import ombruk.backend.calendar.form.EventPostForm
import ombruk.backend.calendar.form.EventUpdateForm
import ombruk.backend.calendar.service.IEventService
import ombruk.backend.shared.api.Authorization
import ombruk.backend.shared.api.Roles
import ombruk.backend.shared.api.generateResponse
import ombruk.backend.shared.api.receiveCatching
import ombruk.backend.shared.error.ValidationError

@KtorExperimentalLocationsAPI
fun Routing.events(eventService: IEventService) {

    get("/events/{event_id}") {
        runCatching { call.parameters["event_id"]?.toInt()!! }
            .fold({ it.right() }, { ValidationError.InputError("Failed to parse event id").left() })
            .flatMap { eventService.getEventByID(it) }
            .run { generateResponse(this) }
            .also { (code, response) -> call.respond(code, response) }
    }

    get<EventGetForm> { form ->
        form.validOrError()
            .flatMap { eventService.getEvents(it) }
            .run { generateResponse(this) }
            .also { (code, response) -> call.respond(code, response) }
    }

    authenticate {
        post("/events/") {
            receiveCatching { call.receive<EventPostForm>() }.flatMap { form ->
                Authorization.authorizeRole(listOf(Roles.RegEmployee), call)
                    .flatMap { form.validOrError() }
                    .flatMap { eventService.saveEvent(it) }
            }
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }
        }
    }

    authenticate {
        patch("/events/") {
            receiveCatching { call.receive<EventUpdateForm>() }.map { form ->
                Authorization.authorizeRole(listOf(Roles.ReuseStation, Roles.RegEmployee), call)
                    .flatMap { form.validOrError() }
                    .map { eventService.updateEvent(it) }
            }
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }
        }
    }

    authenticate {
        delete<EventDeleteForm> { form ->
            Authorization.authorizeRole(listOf(Roles.RegEmployee, Roles.ReuseStation, Roles.Partner), call)
                .flatMap { Authorization.authorizePartnerID(it) { eventService.getEvents(form.toGetForm()) } }
                .flatMap { form.validOrError() }
                .flatMap { eventService.deleteEvent(it) }
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }
        }
    }
}

private fun EventDeleteForm.toGetForm() =
    EventGetForm(eventId, recurrenceRuleId = recurrenceRuleId, fromDate = fromDate, toDate = toDate)