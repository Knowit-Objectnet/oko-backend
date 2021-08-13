package ombruk.backend.henting.application.api.dto


import arrow.core.extensions.either.monad.flatMap
import arrow.core.extensions.either.monadError.ensure
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import ombruk.backend.henting.application.service.IEkstraHentingService
import ombruk.backend.kategori.application.api.dto.EkstraHentingKategoriFindDto
import ombruk.backend.kategori.application.api.dto.EkstraHentingKategoriSaveDto
import ombruk.backend.kategori.application.service.IEkstraHentingKategoriService
import ombruk.backend.shared.api.Authorization
import ombruk.backend.shared.api.Roles
import ombruk.backend.shared.api.generateResponse
import ombruk.backend.shared.api.receiveCatching
import ombruk.backend.shared.error.AuthorizationError


@KtorExperimentalLocationsAPI
fun Routing.ekstraHentinger(ekstraHentingService: IEkstraHentingService) {

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

        route("/med-utlysning") {
            authenticate {
                get<EkstraHentingFindDto> { form ->
                    Authorization.authorizeRole(listOf(Roles.RegEmployee, Roles.ReuseStation, Roles.Partner), call)
                        .flatMap { (role, groupId) ->
                            form.validOrError()
                                .map { if (role == Roles.ReuseStation) it.copy(stasjonId = groupId) else it}
                                .flatMap {
                                    if (role == Roles.Partner) ekstraHentingService.findWithUtlysninger(it, groupId)
                                    else ekstraHentingService.findWithUtlysninger(it)
                                }
                        }
                        .run { generateResponse(this) }
                        .also { (code, response) -> call.respond(code, response) }
                }
            }
        }

        authenticate {
            post {
                Authorization.authorizeRole(listOf(Roles.RegEmployee, Roles.ReuseStation), call)
                    .flatMap { (role, groupId) ->
                        receiveCatching { call.receive<EkstraHentingSaveDto>() }
                        .flatMap { it.validOrError() }
                            .ensure(
                                { AuthorizationError.AccessViolationError("Du har ikke tilgang til denne stasjonen")},
                                { role == Roles.RegEmployee || groupId == it.stasjonId }
                            )
                        .flatMap { ekstraHentingService.save(it) }
                    }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        authenticate {
            delete<EkstraHentingDeleteDto> { form ->
                Authorization.authorizeRole(listOf(Roles.RegEmployee, Roles.ReuseStation), call)
                    .flatMap { (role, groupId) ->
                        form.validOrError()
                            .flatMap { ekstraHentingService.findOne(it.id) }
                            .ensure(
                                { AuthorizationError.AccessViolationError("Du har ikke tilgang til denne hentingen")},
                                {
                                    if (role == Roles.RegEmployee) true
                                    else it.stasjonId == groupId
                                }
                            )
                        .flatMap { ekstraHentingService.archiveOne(form.id) }
                    }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        authenticate {
            patch {
                Authorization.authorizeRole(listOf(Roles.RegEmployee, Roles.ReuseStation), call)
                    .flatMap { (role, groupId) ->
                        receiveCatching { call.receive<EkstraHentingUpdateDto>() }
                            .flatMap { it.validOrError() }
                            .flatMap { dto ->
                                ekstraHentingService.findOne(dto.id)
                                .ensure(
                                    { AuthorizationError.AccessViolationError("Du har ikke tilgang til denne hentingen") },
                                    {
                                        if (role == Roles.RegEmployee) true
                                        else it.stasjonId == groupId
                                    }
                                )
                                .flatMap { ekstraHentingService.update(dto) }
                            }
                    }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }
    }
}