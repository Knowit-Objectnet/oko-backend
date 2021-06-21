package ombruk.backend.utlysning.application.api

import arrow.core.extensions.either.monad.flatMap
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
        get<UtlysningFindOneDto> { form ->
            form.validOrError()
                .flatMap { utlysningService.findOne(form.id) }
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }
        }

        get<UtlysningFindDto> { form ->
            form.validOrError()
                .flatMap { utlysningService.find(form) }
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }
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
            patch<UtlysningPartnerAcceptDto> { form ->
                Authorization.authorizeRole(listOf(Roles.Partner), call)
                    .flatMap { (_, id) ->
                        form.validOrError()
                            .flatMap { utlysningService.findOne(form.id) }
                            .ensure(
                                {AuthorizationError.AccessViolationError("Denne utlysningen tilhører ikke deg")},
                                {it.partnerId == id})
                            .flatMap { utlysningService.partnerAccept(form) }
                    }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        //TODO: If this is functionality we want used, it needs authorization

        authenticate {
            patch<UtlysningStasjonAcceptDto> { form ->
                Authorization.authorizeRole(listOf(Roles.ReuseStation), call)
                    .flatMap { form.validOrError() }
                    .flatMap { utlysningService.stasjonAccept(form) }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }
    }
}