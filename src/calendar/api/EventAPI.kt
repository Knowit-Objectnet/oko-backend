package ombruk.backend.calendar.api

import arrow.core.flatMap
import ombruk.backend.calendar.form.event.EventGetForm
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
import ombruk.backend.calendar.form.event.EventDeleteForm
import ombruk.backend.calendar.form.event.EventGetByIdForm
import ombruk.backend.calendar.form.event.EventPostForm
import ombruk.backend.calendar.form.event.EventUpdateForm
import ombruk.backend.calendar.service.IEventService
import ombruk.backend.shared.api.Authorization
import ombruk.backend.shared.api.Roles
import ombruk.backend.shared.api.generateResponse
import ombruk.backend.shared.api.receiveCatching

@KtorExperimentalLocationsAPI
fun Routing.events(eventService: IEventService) {
    route("/events") {
        get<EventGetByIdForm> { form ->
            form.validOrError()
                .flatMap { eventService.getEventByID(it.id) }
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
            post("/") {
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
            patch("/") {
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
}

private fun EventDeleteForm.toGetForm() =
    EventGetForm(
        eventId,
        recurrenceRuleId = recurrenceRuleId,
        fromDate = fromDate,
        toDate = toDate
    )