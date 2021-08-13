package ombruk.backend.statistikk.application.api

import arrow.core.extensions.either.monad.flatMap
import arrow.core.extensions.either.monadError.ensure
import arrow.core.flatMap
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import ombruk.backend.aktor.application.api.dto.KontaktSaveDto
import ombruk.backend.henting.application.api.dto.HenteplanFindDto
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
        authenticate {
            get<StatistikkFindDto> { form ->
                Authorization.authorizeRole(listOf(Roles.RegEmployee), call)
                    form.validOrError()
                        .flatMap { statistikkService.find(form) }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }
    }
}