package ombruk.backend.statistikk.application.api

import arrow.core.extensions.either.monad.flatMap
import arrow.core.extensions.either.monadError.ensure
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import ombruk.backend.aktor.application.api.dto.KontaktSaveDto
import ombruk.backend.kategori.application.api.dto.*
import ombruk.backend.shared.api.Authorization
import ombruk.backend.shared.api.Roles
import ombruk.backend.shared.api.generateResponse
import ombruk.backend.shared.api.receiveCatching
import ombruk.backend.shared.error.AuthorizationError
import ombruk.backend.statistikk.application.api.dto.StatistikkFindDto
import ombruk.backend.statistikk.application.service.IStatistikkService

@KtorExperimentalLocationsAPI
fun Routing.statistikk(statistikkService: IStatistikkService) {

    route("/statistikk") {
        get<StatistikkFindDto> { form ->
            form.validOrError()
                .flatMap { statistikkService.find(form) }
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }
        }

        //Teststruktur

        route("/vekt") {
            authenticate {
                get {
                    Authorization.authorizeRole(listOf(Roles.RegEmployee), call)
                        .flatMap { receiveCatching { call.receive<StatistikkFindDto>() } }
                        .flatMap { it.validOrError() }
                        .flatMap { statistikkService.find(it) }
                        .run { generateResponse(this) }
                        .also { (code, response) -> call.respond(code, response) }
                }
            }
        }

        route("/stasjon") {

        }

        route("/partner") {

        }

        route("/kategori") {

        }

    }
}