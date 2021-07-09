package ombruk.backend.aarsak.application.api

import arrow.core.extensions.either.monad.flatMap
import arrow.core.extensions.either.monadError.ensure
import arrow.core.flatMap
import io.ktor.application.call
import io.ktor.auth.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.respond
import io.ktor.routing.*
import ombruk.backend.aarsak.application.api.dto.*
import ombruk.backend.aarsak.application.service.IAarsakService
import ombruk.backend.aarsak.domain.enum.AarsakType
import ombruk.backend.aktor.domain.enum.AktorType
import ombruk.backend.henting.application.api.dto.PlanlagtHentingFindDto
import ombruk.backend.shared.api.Authorization
import ombruk.backend.shared.api.Roles
import ombruk.backend.shared.api.generateResponse
import ombruk.backend.shared.api.receiveCatching
import ombruk.backend.shared.error.AuthorizationError

@KtorExperimentalLocationsAPI
fun Routing.aarsak(aarsakService: IAarsakService) {
    route("/aarsak") {
        authenticate {
            get<AarsakFindOneDto> { form ->
                Authorization.authorizeRole(listOf(Roles.RegEmployee, Roles.Partner, Roles.ReuseStation), call)
                    .flatMap { (role, groupId) ->
                        form.validOrError()
                            .flatMap { aarsakService.findOne(it.id) }
                            .ensure(
                                {AuthorizationError.AccessViolationError("Brukeren har ikke tilgang på denne årsaken")},
                                { when (role) {
                                    Roles.RegEmployee -> true
                                    Roles.Partner -> it.type == AarsakType.PARTNER
                                    Roles.ReuseStation -> it.type == AarsakType.STASJON
                                }}
                            )
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
                            .map { it ->
                                it.filter { aarsak ->
                                    when (role) {
                                        Roles.RegEmployee -> true
                                        Roles.Partner -> aarsak.type != AarsakType.STASJON
                                        Roles.ReuseStation -> aarsak.type != AarsakType.PARTNER
                                    }
                                }
                            }
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