package ombruk.backend.utlysning.application.api

import arrow.core.extensions.either.monad.flatMap
import arrow.core.extensions.either.monad.map
import arrow.core.extensions.either.monadError.ensure
import arrow.core.left
import arrow.core.right
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import ombruk.backend.henting.application.service.IEkstraHentingService
import ombruk.backend.shared.api.Authorization
import ombruk.backend.shared.api.Roles
import ombruk.backend.shared.api.generateResponse
import ombruk.backend.shared.api.receiveCatching
import ombruk.backend.shared.error.AuthorizationError
import ombruk.backend.shared.error.RepositoryError
import ombruk.backend.utlysning.application.api.dto.*
import ombruk.backend.utlysning.application.service.IUtlysningService
import java.util.*

@KtorExperimentalLocationsAPI
fun Routing.utlysninger(utlysningService: IUtlysningService, ekstraHentingService: IEkstraHentingService) {

    route("/utlysninger") {

        authenticate {
            get<UtlysningFindOneDto> { form ->
                Authorization.authorizeRole(listOf(Roles.RegEmployee, Roles.ReuseStation, Roles.Partner), call)
                    .flatMap { (role, groupId) ->
                        form.validOrError()
                            .flatMap { utlysningService.findOne(form.id) }
                            .ensure(
                                { AuthorizationError.AccessViolationError("Du har ikke tilgang til denne utlysningen") },
                                {
                                    when (role){
                                        Roles.RegEmployee -> true
                                        Roles.Partner -> it.partnerId == groupId
                                        Roles.ReuseStation -> ekstraHentingService.findOne(it.hentingId)
                                            .fold(
                                                {false},
                                                {it.stasjonId == groupId}
                                            )
                                    }
                                }
                            )
                    }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        authenticate {
            get<UtlysningFindDto> { form ->
                Authorization.authorizeRole(listOf(Roles.RegEmployee, Roles.ReuseStation, Roles.Partner), call)
                    .flatMap { (role, groupId) ->
                        form.validOrError()
                            .map {
                                if (role == Roles.Partner) it.copy(partnerId = groupId) else it
                            }
                            .flatMap { utlysningService.find(it) }
                            .map { utlysningList ->
                                if (role == Roles.ReuseStation)
                                    utlysningList.filter { utlysning ->
                                        ekstraHentingService.findOne(utlysning.hentingId)
                                            .fold(
                                                {false},
                                                {it.stasjonId == groupId}
                                            )
                                    }
                                else utlysningList
                            }
                    }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        get("/godkjent/{ekstraHentingId}") {
            utlysningService.findAccepted(UUID.fromString(call.parameters["ekstraHentingId"]))
                .flatMap { it?.right() ?: RepositoryError.NoRowsFound("Ingen har akseptert").left() }
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }
        }

        authenticate {
            post("/batch") {
                Authorization.authorizeRole(listOf(Roles.RegEmployee, Roles.ReuseStation), call)
                    .flatMap { (role, groupId) ->
                        receiveCatching { call.receive<UtlysningBatchSaveDto>() }
                            .flatMap { it.validOrError() }
                            .flatMap { dto ->
                                ekstraHentingService.findOne(dto.hentingId)
                                    .ensure(
                                        { AuthorizationError.AccessViolationError("Du har ikke tilgang til denne hentingen") },
                                        {
                                            if (role == Roles.RegEmployee) true
                                            else it.stasjonId == groupId
                                        }
                                    )
                                    .flatMap { utlysningService.batchSave(dto) }
                            }
                    }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        authenticate {
            post {
                Authorization.authorizeRole(listOf(Roles.RegEmployee, Roles.ReuseStation), call)
                    .flatMap { (role, groupId) ->
                        receiveCatching { call.receive<UtlysningSaveDto>() }
                            .flatMap { it.validOrError() }
                            .flatMap { dto ->
                                ekstraHentingService.findOne(dto.hentingId)
                                    .ensure(
                                        { AuthorizationError.AccessViolationError("Du har ikke tilgang til denne hentingen") },
                                        {
                                            if (role == Roles.RegEmployee) true
                                            else it.stasjonId == groupId
                                        }
                                    )
                                    .flatMap { utlysningService.save(dto) }
                            }
                    }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        authenticate {
            delete<UtlysningDeleteDto> { form ->
                Authorization.authorizeRole(listOf(Roles.RegEmployee, Roles.ReuseStation), call)
                    .flatMap { (role, groupId) ->
                        form.validOrError()
                            .flatMap { utlysningService.findOne(form.id) }
                            .flatMap { ekstraHentingService.findOne(it.hentingId) }
                            .ensure(
                                { AuthorizationError.AccessViolationError("Du har ikke tilgang til denne hentingen") },
                                {role == Roles.RegEmployee || it.stasjonId == groupId}
                            )
                        .flatMap { utlysningService.archiveOne(form.id) }
                    }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        authenticate {
            patch("/partner-aksepter") {
                Authorization.authorizeRole(listOf(Roles.Partner), call)
                    .flatMap { (_, groupId) ->
                        receiveCatching { call.receive<UtlysningPartnerAcceptDto>() }
                            .flatMap { it.validOrError() }
                            .flatMap { dto ->
                                utlysningService.findOne(dto.id)
                                .ensure(
                                    {AuthorizationError.AccessViolationError("Denne utlysningen tilhører ikke deg")},
                                    {it.partnerId == groupId})
                                .flatMap { utlysningService.partnerAccept(dto) }
                            }
                    }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        authenticate {
            patch("/stasjon-aksepter") {
                Authorization.authorizeRole(listOf(Roles.ReuseStation), call)
                    .flatMap { (_, groupId)  ->
                        receiveCatching { call.receive<UtlysningStasjonAcceptDto>() }
                        .flatMap { it.validOrError() }
                            .ensure(
                                {AuthorizationError.AccessViolationError("Denne utlysningen tilhører ikke deg")},
                                {
                                    utlysningService.findOne(it.id)
                                        .flatMap {
                                            ekstraHentingService.findOne(it.hentingId)
                                                .map { it.stasjonId == groupId }
                                        }.fold({false}, {it})
                                }
                            )
                        .flatMap { utlysningService.stasjonAccept(it) }
                    }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }
    }
}