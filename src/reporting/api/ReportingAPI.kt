package ombruk.backend.reporting.api

import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import ombruk.backend.reporting.form.ReportGetForm
import ombruk.backend.reporting.form.ReportUpdateForm
import ombruk.backend.reporting.service.IReportService
import ombruk.backend.shared.api.Authorization
import ombruk.backend.shared.api.Roles
import ombruk.backend.shared.api.generateResponse
import ombruk.backend.shared.api.receiveCatching
import ombruk.backend.shared.error.RequestError

fun Routing.report(reportService: IReportService) {
    route("/reports") {

        get("/{id}") {
            runCatching { call.parameters["id"]!!.toInt() }
                .fold({ it.right() }, { RequestError.InvalidIdError().left() })
                .flatMap { reportService.getReportById(it) }
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }
        }

        get {
            ReportGetForm.create(call.request.queryParameters)
                .flatMap { reportService.getReports(it) }
                .run { generateResponse(this) }
                .also { (code, response) -> call.respond(code, response) }
        }

        fun patch(auth: Pair<Roles, Int>, reportUpdateForm: ReportUpdateForm) =
            Authorization.authorizeReportPatchByPartnerId(auth) { reportService.getReportById(reportUpdateForm.id) }
                .fold({it.left()}, {reportService.updateReport(reportUpdateForm)})

        authenticate {
            patch {
                receiveCatching { call.receive<ReportUpdateForm>() }.flatMap { patchForm ->
                    Authorization.authorizeRole(listOf(Roles.Partner, Roles.RegEmployee, Roles.ReuseStation), call)
                        .flatMap { patch(it, patchForm) }
                }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

    }


}
