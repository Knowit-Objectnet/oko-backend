package no.oslokommune.ombruk.uttaksdata.api

import arrow.core.flatMap
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.locations.*
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import no.oslokommune.ombruk.uttaksdata.form.UttaksdataGetByIdForm
import no.oslokommune.ombruk.uttaksdata.form.UttaksdataGetForm
import no.oslokommune.ombruk.uttaksdata.form.UttaksdataUpdateForm
import no.oslokommune.ombruk.uttaksdata.service.IUttaksDataService
import no.oslokommune.ombruk.shared.api.Authorization
import no.oslokommune.ombruk.shared.api.Roles
import no.oslokommune.ombruk.shared.api.generateResponse
import no.oslokommune.ombruk.shared.api.receiveCatching
import no.oslokommune.ombruk.uttak.service.IUttakService
import no.oslokommune.ombruk.uttaksdata.form.UttaksdataPostForm

@KtorExperimentalLocationsAPI
fun Routing.uttaksdata(uttaksDataService: IUttaksDataService, uttakService: IUttakService) {
    route("/uttaksdata") {

        get<UttaksdataGetByIdForm> { form ->
            form.validOrError()
                .flatMap { uttaksDataService.getUttaksdataById(it.id) }
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }
        }

        get<UttaksdataGetForm> { form ->
            form.validOrError()
                .flatMap { uttaksDataService.getUttaksdata(it) }
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
                                .flatMap { authorizeUttaksDataByPartnerId(it) { uttakService.getUttakByID(form.id) } }
                        }.flatMap { uttaksDataService.updateUttaksdata(form) }
                    }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

    }


}
