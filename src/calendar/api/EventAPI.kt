package ombruk.backend.calendar.api

import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import ombruk.backend.calendar.form.EventPostForm
import ombruk.backend.calendar.form.EventDeleteForm
import ombruk.backend.calendar.form.EventUpdateForm
import ombruk.backend.calendar.service.IEventService
import ombruk.backend.shared.api.Authorization
import ombruk.backend.shared.api.Roles
import ombruk.backend.shared.api.generateResponse
import ombruk.backend.shared.api.receiveCatching
import ombruk.backend.shared.error.RequestError
import ombruk.calendar.form.api.EventGetForm


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
                    .map { form.validOrError() }
                    .map { eventService.updateEvent(form) }
            }
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