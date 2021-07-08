package ombruk.backend.aarsak.application.api

import arrow.core.flatMap
import io.ktor.application.call
import io.ktor.auth.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.respond
import io.ktor.routing.*
import io.ktor.routing.delete
import ombruk.backend.aarsak.application.api.dto.*
import ombruk.backend.aarsak.application.service.IAarsakService
import ombruk.backend.aktor.application.api.dto.*
import ombruk.backend.aktor.application.service.IAktorService
import ombruk.backend.shared.api.Authorization
import ombruk.backend.shared.api.Roles
import ombruk.backend.shared.api.generateResponse
import ombruk.backend.shared.api.receiveCatching

@KtorExperimentalLocationsAPI
fun Routing.aarsak(aarsakService: IAarsakService) {
    route("/aarsak") {
        authenticate {
            get<AarsakFindOneDto> { form ->
                Authorization.authorizeRole(listOf(Roles.RegEmployee, Roles.Partner, Roles.ReuseStation), call)
                    .flatMap { (role, groupId) ->
                        form.validOrError()
                            .flatMap { aarsakService.findOne(it.id) }
                    }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        authenticate {
            get<AarsakFindDto> { form ->
                Authorization.authorizeRole(listOf(Roles.RegEmployee, Roles.Partner, Roles.ReuseStation), call)
                    .flatMap { (role, groupId) ->
                        form.validOrError()
                            .flatMap { aarsakService.find(it) }
                    }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        authenticate {
            post {
                Authorization.authorizeRole(listOf(Roles.RegEmployee), call)
                    .flatMap { receiveCatching { call.receive<AarsakSaveDto>() } }
                    .flatMap { it.validOrError() }
                    .flatMap { aarsakService.save(it) }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        authenticate {
            patch {
                Authorization.authorizeRole(listOf(Roles.RegEmployee), call)
                    .flatMap { receiveCatching { call.receive<AarsakUpdateDto>() } }
                    .flatMap { it.validOrError() }
                    .flatMap { aarsakService.update(it) }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        authenticate {
            delete<AarsakDeleteDto> { form ->
                Authorization.authorizeRole(listOf(Roles.RegEmployee), call)
                    .flatMap { form.validOrError() }
                    .flatMap { aarsakService.archiveOne(it.id) }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }
    }
}