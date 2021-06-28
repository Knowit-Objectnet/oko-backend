package ombruk.backend.vektregistrering.application.api

import arrow.core.extensions.either.monad.flatMap
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import ombruk.backend.kategori.application.api.dto.KategoriDeleteDto
import ombruk.backend.kategori.application.api.dto.KategoriFindDto
import ombruk.backend.kategori.application.api.dto.KategoriFindOneDto
import ombruk.backend.kategori.application.api.dto.KategoriSaveDto
import ombruk.backend.kategori.application.service.IKategoriService
import ombruk.backend.shared.api.Authorization
import ombruk.backend.shared.api.Roles
import ombruk.backend.shared.api.generateResponse
import ombruk.backend.shared.api.receiveCatching
import ombruk.backend.vektregistrering.application.api.dto.VektregistreringDeleteDto
import ombruk.backend.vektregistrering.application.api.dto.VektregistreringFindDto
import ombruk.backend.vektregistrering.application.api.dto.VektregistreringFindOneDto
import ombruk.backend.vektregistrering.application.api.dto.VektregistreringSaveDto
import ombruk.backend.vektregistrering.application.service.IVektregistreringService

@KtorExperimentalLocationsAPI
fun Routing.vektregistrering(vektregistreringService: IVektregistreringService) {

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
                Authorization.authorizeRole(listOf(Roles.RegEmployee), call)
                    .flatMap { receiveCatching { call.receive<VektregistreringSaveDto>() } }
                    .flatMap { it.validOrError() }
                    .flatMap { vektregistreringService.save(it) }
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