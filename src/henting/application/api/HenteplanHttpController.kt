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
import ombruk.backend.shared.api.Authorization
import ombruk.backend.shared.api.Roles
import ombruk.backend.shared.api.generateResponse
import ombruk.backend.shared.api.receiveCatching

@KtorExperimentalLocationsAPI
fun Routing.henteplaner(henteplanService: IHenteplanService) {

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
                    .flatMap { henteplanService.delete(form) }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        authenticate {
            patch {
                Authorization.authorizeRole(listOf(Roles.RegEmployee), call)
                    .flatMap { receiveCatching { call.receive<HenteplanUpdateDto>() } }
                    .flatMap { it.validOrError() }
                    .flatMap { henteplanService.update(it) }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }
    }
}