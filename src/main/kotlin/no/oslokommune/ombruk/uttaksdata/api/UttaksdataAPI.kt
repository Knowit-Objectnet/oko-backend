package no.oslokommune.ombruk.uttaksdata.api

import arrow.core.flatMap
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.get
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.patch
import io.ktor.routing.route
import no.oslokommune.ombruk.uttaksdata.form.UttaksdataGetByIdForm
import no.oslokommune.ombruk.uttaksdata.form.UttaksdataGetForm
import no.oslokommune.ombruk.uttaksdata.form.UttaksdataUpdateForm
import no.oslokommune.ombruk.uttaksdata.service.IUttaksdataService
import no.oslokommune.ombruk.shared.api.Authorization
import no.oslokommune.ombruk.shared.api.Roles
import no.oslokommune.ombruk.shared.api.generateResponse
import no.oslokommune.ombruk.shared.api.receiveCatching
import no.oslokommune.ombruk.uttak.service.IUttakService

@KtorExperimentalLocationsAPI
fun Routing.uttaksdata(uttaksdataService: IUttaksdataService, uttakService: IUttakService) {
    route("/uttaksdata") {

        get<UttaksdataGetByIdForm> { form ->
            form.validOrError()
                .flatMap { uttaksdataService.getUttaksdataById(it.id) }
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }
        }

        get<UttaksdataGetForm> { form ->
            form.validOrError()
                .flatMap { uttaksdataService.getUttaksdata(it) }
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }
        }

        authenticate {
            patch {
                receiveCatching { call.receive<UttaksdataUpdateForm>() }
                    .flatMap { it.validOrError() }
                    .flatMap { form ->
                        Authorization.run {
                            authorizeRole(listOf(Roles.Partner, Roles.RegEmployee, Roles.ReuseStasjon), call)
                                .flatMap { authorizeReportPatchByPartnerId(it) { uttakService.getUttakByID(form.uttakID) } }
                        }.flatMap { uttaksdataService.updateUttaksdata(form) }
                    }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

    }


}
