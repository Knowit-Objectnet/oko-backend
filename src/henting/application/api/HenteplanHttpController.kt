package ombruk.backend.henting.application.api

import arrow.core.extensions.either.monad.flatMap
import henting.application.api.dto.HenteplanSaveDto
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import ombruk.backend.henting.application.api.dto.*
import ombruk.backend.henting.application.service.IHenteplanService
import ombruk.backend.kategori.application.api.dto.HenteplanKategoriFindDto
import ombruk.backend.kategori.application.api.dto.HenteplanKategoriSaveDto
import ombruk.backend.kategori.application.service.IHenteplanKategoriService
import ombruk.backend.shared.api.Authorization
import ombruk.backend.shared.api.Roles
import ombruk.backend.shared.api.generateResponse
import ombruk.backend.shared.api.receiveCatching
import java.util.*

@KtorExperimentalLocationsAPI
fun Routing.henteplaner(henteplanService: IHenteplanService, henteplanKategoriService: IHenteplanKategoriService) {

    route("/henteplaner") {

        get<HenteplanFindOneDto> { form ->
            form.validOrError()
                .flatMap { henteplanService.findOne(form.id) }
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }
        }

        get<HenteplanFindByAvtaleIdDto> { form ->
            form.validOrError()
                .flatMap { henteplanService.findAllForAvtale(form.avtaleId) }
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }
        }

        get<HenteplanFindDto> { form ->
            form.validOrError()
                .flatMap { henteplanService.find(form) }
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }
        }

        authenticate {
            post {
                Authorization.authorizeRole(listOf(Roles.RegEmployee), call)
                    .flatMap { receiveCatching { call.receive<HenteplanSaveDto>() } }
                    .flatMap { it.validOrError() }
                    .flatMap { henteplanService.save(it) }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        authenticate {
            delete<HenteplanDeleteDto> { form ->
                Authorization.authorizeRole(listOf(Roles.RegEmployee), call)
                    .flatMap { form.validOrError() }
                    .flatMap { henteplanService.archiveOne(form.id) }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        authenticate {
            patch<HenteplanUpdateDto> { form ->
                Authorization.authorizeRole(listOf(Roles.RegEmployee), call)
                    .flatMap { form.validOrError() }
                    .flatMap { henteplanService.update(form) }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }


        authenticate {
            post("/{henteplanId}/kategorier") {
                Authorization.authorizeRole(listOf(Roles.RegEmployee), call)
                    .flatMap { receiveCatching { call.receive<HenteplanKategoriSaveDto>() } }
                    .flatMap { it.validOrError() }
                    .flatMap { henteplanKategoriService.save(it) }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        get<HenteplanKategoriFindDto> { form ->
            println("form: $form")
            form.validOrError()
                .flatMap { henteplanKategoriService.find(form) }
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }
        }
    }
}