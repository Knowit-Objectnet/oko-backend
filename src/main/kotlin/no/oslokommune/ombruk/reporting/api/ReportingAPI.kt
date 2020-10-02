package no.oslokommune.ombruk.reporting.api

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
import no.oslokommune.ombruk.reporting.form.ReportGetByIdForm
import no.oslokommune.ombruk.reporting.form.ReportGetForm
import no.oslokommune.ombruk.reporting.form.ReportUpdateForm
import no.oslokommune.ombruk.reporting.service.IReportService
import no.oslokommune.ombruk.shared.api.Authorization
import no.oslokommune.ombruk.shared.api.Roles
import no.oslokommune.ombruk.shared.api.generateResponse
import no.oslokommune.ombruk.shared.api.receiveCatching

@KtorExperimentalLocationsAPI
fun Routing.report(reportService: IReportService) {
    route("/reports") {

        get<ReportGetByIdForm> { form ->
            form.validOrError()
                .flatMap { reportService.getReportById(it.id) }
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }
        }

        get<ReportGetForm> { form ->
            form.validOrError()
                .flatMap { reportService.getReports(it) }
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }
        }

        authenticate {
            patch {
                receiveCatching { call.receive<ReportUpdateForm>() }
                    .flatMap { it.validOrError() }
                    .flatMap { form ->
                        Authorization.run {
                            authorizeRole(listOf(Roles.Partner, Roles.RegEmployee, Roles.ReuseStasjon), call)
                                .flatMap { authorizeReportPatchByPartnerId(it) { reportService.getReportById(form.id) } }
                        }.flatMap { reportService.updateReport(form) }
                    }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

    }


}
