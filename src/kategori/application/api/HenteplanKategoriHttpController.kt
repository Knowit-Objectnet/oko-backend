package ombruk.backend.kategori.application.api

import arrow.core.extensions.either.monad.flatMap
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import ombruk.backend.kategori.application.api.dto.*
import ombruk.backend.kategori.application.service.IHenteplanKategoriService
import ombruk.backend.shared.api.Authorization
import ombruk.backend.shared.api.Roles
import ombruk.backend.shared.api.generateResponse
import ombruk.backend.shared.api.receiveCatching

@KtorExperimentalLocationsAPI
fun Routing.henteplanKategorier(henteplanKategoriService: IHenteplanKategoriService) {

    route("/henteplankategorier") {
        get<HenteplanKategoriFindOneDto> { form ->
            form.validOrError()
                .flatMap { henteplanKategoriService.findOne(form.id) }
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }
        }

        get<HenteplanKategoriFindDto> { form ->
            form.validOrError()
                .flatMap { henteplanKategoriService.find(form) }
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }
        }

        authenticate {
            post {
                Authorization.authorizeRole(listOf(Roles.RegEmployee), call)
                    .flatMap { receiveCatching { call.receive<HenteplanKategoriSaveDto>() } }
                    .flatMap { it.validOrError() }
                    .flatMap { henteplanKategoriService.save(it) }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        authenticate {
            delete<HenteplanKategoriDeleteDto> { form ->
                Authorization.authorizeRole(listOf(Roles.RegEmployee), call)
                    .flatMap { form.validOrError() }
                    .flatMap { henteplanKategoriService.delete(form) }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

    }
}