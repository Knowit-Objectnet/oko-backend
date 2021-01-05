package no.oslokommune.ombruk.uttaksdata.api

import arrow.core.flatMap
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.locations.*
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import no.oslokommune.ombruk.uttaksdata.form.UttaksDataGetByIdForm
import no.oslokommune.ombruk.uttaksdata.form.UttaksDataGetForm
import no.oslokommune.ombruk.uttaksdata.form.UttaksDataUpdateForm
import no.oslokommune.ombruk.uttaksdata.service.IUttaksDataService
import no.oslokommune.ombruk.shared.api.Authorization
import no.oslokommune.ombruk.shared.api.Roles
import no.oslokommune.ombruk.shared.api.generateResponse
import no.oslokommune.ombruk.shared.api.receiveCatching
import no.oslokommune.ombruk.uttak.service.IUttakService

@KtorExperimentalLocationsAPI
fun Routing.uttaksdata(uttaksDataService: IUttaksDataService, uttakService: IUttakService) {
    route("/uttaksdata") {

        get<UttaksDataGetByIdForm> { form ->
            form.validOrError()
                .flatMap { uttaksDataService.getUttaksDataById(it.id) }
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }
        }

        get<UttaksDataGetForm> { form ->
            form.validOrError()
                .flatMap { uttaksDataService.getUttaksData(it) }
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }
        }

        authenticate {
            patch {
                receiveCatching { call.receive<UttaksDataUpdateForm>() }
                    .flatMap { it.validOrError() }
                    .flatMap { form ->
                        Authorization.run {
                            authorizeRole(listOf(Roles.Partner, Roles.RegEmployee, Roles.ReuseStasjon), call)
                                .flatMap { authorizeUttaksDataByPartnerId(it) { uttakService.getUttakByID(form.uttakId) } }
                        }.flatMap { uttaksDataService.updateUttaksData(form) }
                    }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

    }


}
