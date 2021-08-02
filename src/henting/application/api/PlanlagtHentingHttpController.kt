package ombruk.backend.henting.application.api

import arrow.core.extensions.either.monad.flatMap
import arrow.core.extensions.either.monadError.ensure
import arrow.core.flatMap
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import ombruk.backend.henting.application.api.dto.*
import ombruk.backend.henting.application.service.IPlanlagtHentingService
import ombruk.backend.shared.api.Authorization
import ombruk.backend.shared.api.Roles
import ombruk.backend.shared.api.generateResponse
import ombruk.backend.shared.api.receiveCatching
import ombruk.backend.shared.error.AuthorizationError

@KtorExperimentalLocationsAPI
fun Routing.planlagteHentinger(planlagtHentingService: IPlanlagtHentingService) {

    route("/planlagte-hentinger") {

        authenticate {
            get<PlanlagtHentingFindOneDto> { form ->
                Authorization.authorizeRole(listOf(Roles.RegEmployee, Roles.Partner, Roles.ReuseStation), call)
                    .flatMap { (role, groupId) ->
                        form.validOrError()
                            .flatMap { planlagtHentingService.findOne(form.id) }
                            .ensure(
                                { AuthorizationError.AccessViolationError("Hentingen er ikke tilgjengelig for deg")},
                                { when (role) {
                                    Roles.Partner -> it.aktorId == groupId
                                    Roles.ReuseStation -> it.stasjonId == groupId
                                    Roles.RegEmployee -> true
                                    }
                                }
                            )
                    }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        authenticate {
            get<PlanlagtHentingFindDto> { form ->
                Authorization.authorizeRole(listOf(Roles.RegEmployee, Roles.Partner, Roles.ReuseStation), call)
                    .flatMap { (role, groupId) ->
                        form.validOrError()
                            .map { when (role) {
                                Roles.Partner -> it.copy(aktorId = groupId)
                                Roles.ReuseStation -> it.copy(stasjonId = groupId)
                                Roles.RegEmployee -> it
                            } }
                            .flatMap { planlagtHentingService.find(it) }
                    }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        //TODO: Determine how to do PlanlagtHentingWithParents - use it as default?

        authenticate {
            post {
                Authorization.authorizeRole(listOf(Roles.RegEmployee), call)
                    .flatMap { receiveCatching { call.receive<PlanlagtHentingSaveDto>() } }
                    .flatMap { it.validOrError() }
                    .flatMap { planlagtHentingService.save(it) }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        authenticate {
            delete<PlanlagtHentingDeleteDto> { form ->
                Authorization.authorizeRole(listOf(Roles.RegEmployee), call)
                    .flatMap { form.validOrError() }
                    .flatMap { planlagtHentingService.archiveOne(form.id) }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        authenticate {
            patch{
                Authorization.authorizeRole(listOf(Roles.RegEmployee, Roles.Partner, Roles.ReuseStation), call)
                    .flatMap { (role, groupId) ->
                        receiveCatching { call.receive<PlanlagtHentingUpdateDto>() }
                            .flatMap { it.validOrError() }
                            .flatMap { dto ->
                                planlagtHentingService.findOne(dto.id)
                                .ensure(
                                    {AuthorizationError.AccessViolationError("Planlagt henting ikke tillatt å endre av denne gruppen")},
                                    {
                                        when (role) {
                                            Roles.RegEmployee -> true
                                            Roles.Partner -> groupId == it.aktorId
                                            Roles.ReuseStation -> groupId == it.stasjonId || groupId == it.aktorId
                                        }
                                    }
                                )
                                    .flatMap { dto.avlyst?.run { planlagtHentingService.update(dto, groupId)} ?: planlagtHentingService.update(dto) }
                            }
                    }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }
    }
}