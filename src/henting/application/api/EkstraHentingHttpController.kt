package ombruk.backend.henting.application.api.dto

import arrow.core.extensions.either.monad.flatMap
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import ombruk.backend.henting.application.service.IEkstraHentingService
import ombruk.backend.kategori.application.api.dto.EkstraHentingKategoriFindDto
import ombruk.backend.kategori.application.api.dto.EkstraHentingKategoriSaveDto
import ombruk.backend.kategori.application.service.EkstraHentingKategoriService
import ombruk.backend.kategori.application.service.IEkstraHentingKategoriService
import ombruk.backend.shared.api.Authorization
import ombruk.backend.shared.api.Roles
import ombruk.backend.shared.api.generateResponse
import ombruk.backend.shared.api.receiveCatching
import javax.management.relation.Role

@KtorExperimentalLocationsAPI
fun Routing.ekstraHentinger(ekstraHentingService: IEkstraHentingService, ekstraHentingKategoriService: IEkstraHentingKategoriService) {

    route("/ekstra-hentinger") {

        get<EkstraHentingFindOneDto> { form ->
            form.validOrError()
                .flatMap { ekstraHentingService.findOne(form.id) }
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }
        }

        get<EkstraHentingFindDto> { form ->
            form.validOrError()
                .flatMap { ekstraHentingService.find(form) }
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }
        }

        authenticate {
            post {
                Authorization.authorizeRole(listOf(Roles.RegEmployee), call)
                    .flatMap { receiveCatching { call.receive<EkstraHentingSaveDto>() } }
                    .flatMap { it.validOrError() }
                    .flatMap { ekstraHentingService.save(it) }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        authenticate {
            delete<EkstraHentingDeleteDto> { form ->
                Authorization.authorizeRole(listOf(Roles.RegEmployee), call)
                    .flatMap { form.validOrError() }
                    .flatMap { ekstraHentingService.archiveOne(form.id) }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        authenticate {
            patch<EkstraHentingUpdateDto> { form ->
                Authorization.authorizeRole(listOf(Roles.RegEmployee), call)
                    .flatMap { form.validOrError() }
                    .flatMap { ekstraHentingService.update(form) }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        authenticate {
            post("/{ekstrahentingId}/kategorier") {
                Authorization.authorizeRole(listOf(Roles.RegEmployee), call)
                    .flatMap { receiveCatching { call.receive<EkstraHentingKategoriSaveDto>() } }
                    .flatMap { it.validOrError() }
                    .flatMap { ekstraHentingKategoriService.save(it) }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        get<EkstraHentingKategoriFindDto> {form ->
            println("form $form")
            form.validOrError()
                .flatMap { ekstraHentingKategoriService.find(form) }
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }
        }
    }
}