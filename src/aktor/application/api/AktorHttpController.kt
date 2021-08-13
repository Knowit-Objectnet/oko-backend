package ombruk.backend.aktor.application.api

import arrow.core.flatMap
import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.*
import ombruk.backend.aktor.application.api.dto.*
import ombruk.backend.aktor.application.service.IAktorService
import ombruk.backend.shared.api.generateResponse

@KtorExperimentalLocationsAPI
fun Routing.aktor(aktorService: IAktorService) {
    route("/aktor") {
        get<AktorFindOneDto> { form ->
            form.validOrError()
                .flatMap { aktorService.findOne(it.id) }
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }
        }
    }
}