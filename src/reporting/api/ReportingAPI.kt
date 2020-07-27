package ombruk.backend.reporting.api

import arrow.core.Either
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import ombruk.backend.shared.error.RepositoryError
import ombruk.backend.reporting.model.Report
import ombruk.backend.reporting.service.IReportService

fun Routing.report(reportService: IReportService) {
    route("/reports") {

        get("/{id}") {
            val id = runCatching { call.parameters["id"]!!.toInt() }.getOrElse {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                return@get
            }

            when (val result = reportService.getReportById(id)) {
                is Either.Left -> when (result.a) {
                    is RepositoryError.NoRowsFound -> call.respond(HttpStatusCode.NotFound)
                    else -> call.respond(HttpStatusCode.InternalServerError)
                }
                is Either.Right -> call.respond(HttpStatusCode.OK, result.b)
            }
        }

        get("/partner/{partnerID}") {
            val partnerID = runCatching { call.parameters["partnerID"]!!.toInt() }.getOrElse {
                call.respond(HttpStatusCode.BadRequest, "Invalid partner ID")
                return@get
            }

            when (val result = reportService.getReportsByPartnerId(partnerID)) {
                is Either.Left -> when (result.a) {
                    is RepositoryError.NoRowsFound -> call.respond(HttpStatusCode.NotFound)
                    else -> call.respond(HttpStatusCode.InternalServerError)
                }
                is Either.Right -> call.respond(HttpStatusCode.OK, result.b)
            }
        }

        get {
            when (val result = reportService.getReports()) {
                is Either.Left -> when (result.a) {
                    is RepositoryError.NoRowsFound -> call.respond(HttpStatusCode.NotFound)
                    else -> call.respond(HttpStatusCode.InternalServerError)
                }
                is Either.Right -> call.respond(HttpStatusCode.OK, result.b)
            }
        }
    }


}
