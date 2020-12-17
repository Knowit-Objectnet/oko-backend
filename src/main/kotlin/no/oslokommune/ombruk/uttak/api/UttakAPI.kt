package no.oslokommune.ombruk.uttak.api

import arrow.core.flatMap
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.delete
import io.ktor.locations.get
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import no.oslokommune.ombruk.uttak.form.*
import no.oslokommune.ombruk.uttak.service.IUttakService
import no.oslokommune.ombruk.shared.api.generateResponse
import no.oslokommune.ombruk.shared.api.receiveCatching
import no.oslokommune.ombruk.shared.api.Authorization
import no.oslokommune.ombruk.shared.api.Roles
import no.oslokommune.ombruk.uttak.service.UttakService
import no.oslokommune.ombruk.uttak.service.UttakService.deleteUttak
import no.oslokommune.ombruk.uttaksdata.service.IUttaksDataService

@KtorExperimentalLocationsAPI
fun Routing.uttak(uttakService: IUttakService, uttaksDataService: IUttaksDataService) {
    route("/uttak") {
        get<UttakGetByIdForm> { form ->
            form.validOrError()
                .flatMap { uttakService.getUttakByID(it.id) }
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }
        }

        get<UttakGetForm> { form ->
                form.validOrError()
                    .flatMap { uttakService.getUttak(it) }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }

        authenticate {
            post("/") {
                receiveCatching { call.receive<UttakPostForm>() }.flatMap { form ->
                    Authorization.authorizeRole(listOf(Roles.RegEmployee), call)
                        .flatMap { form.validOrError() }
                        .flatMap { uttakService.saveUttak(it) }
                }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        authenticate {
            patch("/") {
                receiveCatching { call.receive<UttakUpdateForm>() }.flatMap { form ->
                    Authorization.authorizeRole(listOf(Roles.ReuseStasjon, Roles.RegEmployee), call)
                        .flatMap { form.validOrError() }
                        .flatMap { uttakService.updateUttak(it) }
                }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        authenticate {
            delete<UttakDeleteForm> { form ->
                Authorization.authorizeRole(listOf(Roles.RegEmployee, Roles.ReuseStasjon, Roles.Partner), call)
                    .map { if (it.first == Roles.Partner) form.partnerId = it.second; it }
                    .flatMap { Authorization.authorizePartnerID(it) {UttakService.getUttak(form.toGetForm())} }
                    .flatMap { form.validOrError() }
                    .flatMap { deleteUttak(form) }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
//                Authorization.authorizeRole(listOf(Roles.RegEmployee, Roles.ReuseStasjon, Roles.Partner), call)
//                    .flatMap { auth ->
//                        form.validOrError()
//                            .flatMap {
//                                Authorization.authorizePartnerID(auth) {
//                                    uttakService.getUttak(form.toGetForm())
//                                }
//                                .flatMap { uttakList -> deleteUttak(uttakList) }
//                            }
//                    }
//                    .run { generateResponse(this) }
//                    .also { (code, response) -> call.respond(code, response) }

            }
        }

        /*
        authenticate {
            delete<UttakDeleteForm> { form ->
                Authorization.authorizeRole(listOf(Roles.RegEmployee, Roles.ReuseStasjon, Roles.Partner), call)
                    .map {
                        Authorization.authorizePartnerID(it) {
                            form.validOrError()
                                .flatMap { uttakService.getUttak(form.toGetForm()) }
                        }
                        .flatMap { uttakList -> deleteUttak(uttakList) }
                                /*
                        .flatMap { uttakList ->
                            uttakList.map { uttak ->
                                uttakService.deleteUttak(uttak)
                            }.right()
                        }

                                 */
                    }
                    .run { generateResponse(this) }
                    .also { (code, response) ->
                        print("aaaa")
                        call.respond(code)
                    }
            }
        }

         */

        /*
        authenticate {
            delete<UttakDeleteForm> { form ->
                Authorization.authorizeRole(listOf(Roles.RegEmployee, Roles.ReuseStasjon, Roles.Partner), call)
                    .map { if (it.first == Roles.Partner) form.partnerId = it.second; it }
                    .flatMap {
                        Authorization.authorizePartnerID(it) {
                            form.validOrError()
                            .flatMap { uttakService.getUttak(form.toGetForm()) }
                        }
                    }
                    //.flatMap { form.validOrError() }
                    .flatMap { uttakService.deleteUttak() }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }
        */
    }
}


@KtorExperimentalLocationsAPI
private fun UttakDeleteForm.toGetForm() =
    UttakGetForm(
        id,
        gjentakelsesRegelID = gjentakelsesRegelId,
        startTidspunkt = startTidspunkt,
        sluttTidspunkt = sluttTidspunkt,
        stasjonId = stasjonId,
        partnerId = partnerId
    )