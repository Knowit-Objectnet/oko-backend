package ombruk.backend.pickup.api

import arrow.core.flatMap
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.delete
import io.ktor.locations.get
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.post
import io.ktor.routing.route
import ombruk.backend.pickup.form.request.RequestDeleteForm
import ombruk.backend.pickup.form.request.RequestGetForm
import ombruk.backend.pickup.form.request.RequestPostForm
import ombruk.backend.pickup.service.IRequestService
import ombruk.backend.shared.api.Authorization
import ombruk.backend.shared.api.Roles
import ombruk.backend.shared.api.generateResponse
import ombruk.backend.shared.api.receiveCatching


@KtorExperimentalLocationsAPI
fun Routing.request(requestService: IRequestService) {

    route("/requests") {

        authenticate {
            post {
                receiveCatching { call.receive<RequestPostForm>() }.flatMap { form ->
                    Authorization.authorizeRole(listOf(Roles.Partner), call)
                        .flatMap { Authorization.authorizeRequestId(it, form.partnerId) }
                        .flatMap { form.validOrError() }
                        .flatMap { requestService.saveRequest(it) }
                }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }


        get<RequestGetForm> { form ->
            form.validOrError()
                .flatMap { requestService.getRequests(form) }
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }
        }

        authenticate {
            delete<RequestDeleteForm> { form ->
                Authorization.authorizeRole(listOf(Roles.Partner), call)
                    .flatMap { Authorization.authorizeRequestId(it, form.partnerId) }
                    .flatMap { form.validOrError() }
                    .flatMap { requestService.deleteRequest(form) }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }
    }
}