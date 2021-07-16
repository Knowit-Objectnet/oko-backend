package ombruk.backend.henting.application.api.dto


import arrow.core.extensions.either.monad.flatMap
import io.ktor.application.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import ombruk.backend.henting.application.service.IHentingService
import ombruk.backend.shared.api.generateResponse


@KtorExperimentalLocationsAPI
fun Routing.hentinger(hentingService: IHentingService) {

    route("/hentinger") {

        get<HentingFindOneDto> { form ->
            form.validOrError()
                .flatMap { hentingService.findOne(form.id) }
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }
        }

        get<HentingFindDto> { form ->
            form.validOrError()
                .flatMap { hentingService.find(form) }
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }
        }

    }
}