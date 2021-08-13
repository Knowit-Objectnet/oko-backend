package ombruk.backend.vektregistrering.application.api

import arrow.core.Either
import arrow.core.extensions.either.applicative.applicative
import arrow.core.extensions.either.monad.flatMap
import arrow.core.extensions.either.monadError.ensure
import arrow.core.extensions.list.traverse.sequence
import arrow.core.fix
import arrow.core.left
import arrow.core.right
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import ombruk.backend.henting.application.service.IHentingService
import ombruk.backend.henting.domain.entity.EkstraHenting
import ombruk.backend.henting.domain.entity.PlanlagtHenting
import ombruk.backend.henting.domain.model.HentingType
import ombruk.backend.shared.api.Authorization
import ombruk.backend.shared.api.Roles
import ombruk.backend.shared.api.generateResponse
import ombruk.backend.shared.api.receiveCatching
import ombruk.backend.shared.error.AuthorizationError
import ombruk.backend.shared.error.ServiceError
import ombruk.backend.utlysning.application.api.dto.UtlysningBatchSaveDto
import ombruk.backend.vektregistrering.application.api.dto.*
import ombruk.backend.vektregistrering.application.service.IVektregistreringService
import org.h2.mvstore.MVStoreTool.rollback
import java.util.*

@KtorExperimentalLocationsAPI
fun Routing.vektregistrering(vektregistreringService: IVektregistreringService, hentingService: IHentingService) {

    route("/vektregistrering") {
        get<VektregistreringFindOneDto> { form ->
            form.validOrError()
                .flatMap { vektregistreringService.findOne(form.id) }
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }
        }

        get<VektregistreringFindDto> { form ->
            form.validOrError()
                .flatMap { vektregistreringService.find(form) }
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }
        }

        authenticate {
            post {
                Authorization.authorizeRole(listOf(Roles.RegEmployee, Roles.Partner, Roles.ReuseStation), call)
                    .flatMap { (role, groupId) ->
                        receiveCatching { call.receive<VektregistreringSaveDto>() }
                            .flatMap { it.validOrError() }
                            .flatMap { dto ->
                                hentingService.findOne(dto.hentingId)
                                    .ensure(
                                        { AuthorizationError.AccessViolationError("Vektregistrering ikke tillatt av denne gruppen")},
                                        {
                                            when (role) {
                                                Roles.RegEmployee -> true
                                                Roles.Partner -> {
                                                    when (it.type) {
                                                        HentingType.PLANLAGT -> groupId == it.aktorId
                                                        HentingType.EKSTRA -> groupId == it.aktorId
                                                    }
                                                }
                                                Roles.ReuseStation -> {
                                                    when (it.type) {
                                                        HentingType.PLANLAGT -> groupId == it.stasjonId || groupId == it.aktorId
                                                        HentingType.EKSTRA -> groupId == it.stasjonId
                                                    }
                                                }
                                            }
                                        }
                                    )
                                    .flatMap { vektregistreringService.save(dto) }
                            }
                    }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        authenticate {
            post("/batch") {
                Authorization.authorizeRole(listOf(Roles.RegEmployee, Roles.ReuseStation, Roles.Partner), call)
                    .flatMap { (role, groupId) ->
                        receiveCatching { call.receive<VektregistreringBatchSaveDto>() }
                            .flatMap { it.validOrError() }
                            .flatMap { dto ->
                                hentingService.findOne(dto.hentingId)
                                    .ensure(
                                        { AuthorizationError.AccessViolationError("Vektregistrering ikke tillatt av denne gruppen")},
                                        {
                                            when (role) {
                                                Roles.RegEmployee -> true
                                                Roles.Partner -> {
                                                    when (it.type) {
                                                        HentingType.PLANLAGT -> groupId == it.aktorId
                                                        HentingType.EKSTRA -> groupId == it.aktorId
                                                    }
                                                }
                                                Roles.ReuseStation -> {
                                                    when (it.type) {
                                                        HentingType.PLANLAGT -> groupId == it.stasjonId || groupId == it.aktorId
                                                        HentingType.EKSTRA -> groupId == it.stasjonId
                                                    }
                                                }
                                            }
                                        }
                                    )
                                    .flatMap { vektregistreringService.batchSave(dto) }
                            }
                    }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        authenticate {
            patch {
                Authorization.authorizeRole(listOf(Roles.RegEmployee, Roles.Partner, Roles.ReuseStation), call)
                    .flatMap { (role, groupId) ->
                        receiveCatching { call.receive<VektregistreringUpdateDto>() }
                            .flatMap { it.validOrError() }
                            .flatMap { dto ->
                                vektregistreringService.findOne(dto.id).flatMap {
                                    hentingService.findOne(it.hentingId)
                                        .ensure(
                                            { AuthorizationError.AccessViolationError("Endring av vektregistrering er ikke tillatt av denne gruppen")},
                                            {
                                                when (role) {
                                                    Roles.RegEmployee -> true
                                                    Roles.Partner -> {
                                                        when (it.type) {
                                                            HentingType.PLANLAGT -> groupId == it.aktorId
                                                            HentingType.EKSTRA -> groupId == it.aktorId
                                                        }
                                                    }
                                                    Roles.ReuseStation -> {
                                                        when (it.type) {
                                                            HentingType.PLANLAGT -> groupId == it.stasjonId || groupId == it.aktorId
                                                            HentingType.EKSTRA -> groupId == it.stasjonId
                                                        }
                                                    }
                                                }
                                            }
                                        ).flatMap { vektregistreringService.update(dto) }
                                }
                            }
                    }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        authenticate {
            patch("/batch") {
                Authorization.authorizeRole(listOf(Roles.RegEmployee, Roles.ReuseStation, Roles.Partner), call)
                    .flatMap { (role, groupId) ->
                        receiveCatching { call.receive<VektregistreringBatchUpdateDto>() }
                            .flatMap { it.validOrError() }
                            .flatMap { dto ->
                                dto.vektregistreringIds.mapIndexed { index, vId ->
                                    vektregistreringService.findOne(UUID.fromString(vId))
                                        .flatMap { hentingService.findOne(it.hentingId)
                                            .ensure(
                                                {AuthorizationError.AccessViolationError("Henting er ikke tillatt å endre av denne gruppen")},
                                                {
                                                    when (role) {
                                                        Roles.RegEmployee -> true
                                                        Roles.Partner -> {
                                                            when (it.type) {
                                                                HentingType.PLANLAGT -> groupId == it.aktorId
                                                                HentingType.EKSTRA -> groupId == it.aktorId
                                                            }
                                                        }
                                                        Roles.ReuseStation -> {
                                                            when (it.type) {
                                                                HentingType.PLANLAGT -> groupId == it.stasjonId || groupId == it.aktorId
                                                                HentingType.EKSTRA -> groupId == it.stasjonId
                                                            }
                                                        }
                                                    }
                                                }
                                            )
                                        }
                                }.sequence(Either.applicative()).fix().map { it.fix() }.fold({it.left()},{ vektregistreringService.batchUpdate(dto) })
                            }
                    }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        authenticate {
            delete<VektregistreringDeleteDto> { form ->
                Authorization.authorizeRole(listOf(Roles.RegEmployee), call)
                    .flatMap { form.validOrError() }
                    .flatMap { vektregistreringService.delete(form.id) }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }
    }
}