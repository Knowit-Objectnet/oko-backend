package ombruk.backend.vektregistrering.application.api

import arrow.core.extensions.either.monad.flatMap
import arrow.core.extensions.either.monadError.ensure
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import ombruk.backend.henting.application.api.dto.PlanlagtHentingUpdateDto
import ombruk.backend.henting.application.service.IHentingService
import ombruk.backend.henting.domain.entity.EkstraHenting
import ombruk.backend.henting.domain.entity.PlanlagtHentingWithParents
import ombruk.backend.kategori.application.api.dto.KategoriDeleteDto
import ombruk.backend.kategori.application.api.dto.KategoriFindDto
import ombruk.backend.kategori.application.api.dto.KategoriFindOneDto
import ombruk.backend.kategori.application.api.dto.KategoriSaveDto
import ombruk.backend.kategori.application.service.IKategoriService
import ombruk.backend.shared.api.Authorization
import ombruk.backend.shared.api.Roles
import ombruk.backend.shared.api.generateResponse
import ombruk.backend.shared.api.receiveCatching
import ombruk.backend.shared.error.AuthorizationError
import ombruk.backend.vektregistrering.application.api.dto.VektregistreringDeleteDto
import ombruk.backend.vektregistrering.application.api.dto.VektregistreringFindDto
import ombruk.backend.vektregistrering.application.api.dto.VektregistreringFindOneDto
import ombruk.backend.vektregistrering.application.api.dto.VektregistreringSaveDto
import ombruk.backend.vektregistrering.application.service.IVektregistreringService

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
                                                    if (it is PlanlagtHentingWithParents) groupId == it.aktorId
                                                    else groupId == (it as EkstraHenting).godkjentUtlysning?.partnerId
                                                }
                                                Roles.ReuseStation -> {
                                                    if (it is PlanlagtHentingWithParents) groupId == it.stasjonId || groupId == it.aktorId
                                                    else groupId == (it as EkstraHenting).stasjonId
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