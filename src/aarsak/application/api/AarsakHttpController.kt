package ombruk.backend.aarsak.application.api

import arrow.core.flatMap
import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.*
import ombruk.backend.aarsak.application.api.dto.AarsakFindOneDto
import ombruk.backend.aarsak.application.service.IAarsakService
import ombruk.backend.aktor.application.api.dto.*
import ombruk.backend.aktor.application.service.IAktorService
import ombruk.backend.shared.api.generateResponse

@KtorExperimentalLocationsAPI
fun Routing.aarsak(aarsakService: IAarsakService) {
    route("/aarsak") {
        get<AarsakFindOneDto> { form ->
            form.validOrError()
                .flatMap { aarsakService.findOne(it.id) }
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }
        }
    }
}