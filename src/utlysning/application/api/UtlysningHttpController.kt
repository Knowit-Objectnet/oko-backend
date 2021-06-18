package ombruk.backend.utlysning.application.api

import arrow.core.extensions.either.monad.flatMap
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import ombruk.backend.shared.api.Authorization
import ombruk.backend.shared.api.Roles
import ombruk.backend.shared.api.generateResponse
import ombruk.backend.shared.api.receiveCatching
import ombruk.backend.utlysning.application.api.dto.*
import ombruk.backend.utlysning.application.service.IUtlysningService
import java.util.*

@KtorExperimentalLocationsAPI
fun Routing.utlysnigner(utlysningService: IUtlysningService) {

    route("/utlysninger") {
        get<UtlysningFindOneDto> { form ->
            form.validOrError()
                .flatMap { utlysningService.findOne(form.id) }
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }
        }

        get<UtlysningFindDto> { form ->
            form.validOrError()
                .flatMap { utlysningService.find(form) }
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }
        }

        get("/godkjent/{ekstraHentingId}") {
            utlysningService.findAccepted(UUID.fromString(call.parameters["ekstraHentingId"]))
                .flatMap { it?.right() ?: RepositoryError.NoRowsFound("Ingen har akseptert").left() }
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }
        }

        authenticate {
            post {
                Authorization.authorizeRole(listOf(Roles.RegEmployee), call)
                    .flatMap { receiveCatching { call.receive<UtlysningSaveDto>() } }
                    .flatMap { it.validOrError() }
                    .flatMap { utlysningService.save(it) }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        authenticate {
            delete<UtlysningDeleteDto> { form ->
                Authorization.authorizeRole(listOf(Roles.RegEmployee), call)
                    .flatMap { form.validOrError() }
                    .flatMap { utlysningService.archiveOne(form.id) }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        //TODO: Validate that the Partner/Stasjon is the correct one

        authenticate {
            patch<UtlysningPartnerAcceptDto> { form ->
                Authorization.authorizeRole(listOf(Roles.Partner), call)
                    .flatMap { form.validOrError() }
                    .flatMap { utlysningService.partnerAccept(form) }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        authenticate {
            patch<UtlysningStasjonAcceptDto> { form ->
                Authorization.authorizeRole(listOf(Roles.ReuseStation), call)
                    .flatMap { form.validOrError() }
                    .flatMap { utlysningService.stasjonAccept(form) }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }
    }
}