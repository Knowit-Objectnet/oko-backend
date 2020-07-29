package ombruk.backend.reporting.form

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.ktor.http.Parameters
import ombruk.backend.shared.error.ValidationError
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ReportGetForm private constructor(
    var eventID: Int? = null,
    var stationID: Int? = null,
    var partnerID: Int? = null,
    var fromDate: LocalDateTime? = null,
    var toDate: LocalDateTime? = null
) {
    companion object {
        fun create(params: Parameters): Either<ValidationError, ReportGetForm> {
            val report = ReportGetForm()
            params["event-id"]?.let { param ->
                kotlin.runCatching { param.toInt() }
                    .onSuccess { report.eventID = it }
                    .onFailure { return ValidationError.InputError("station-id must be an Int, is: $param").left() }
            }
            params["station-id"]?.let { param ->
                kotlin.runCatching { param.toInt() }
                    .onSuccess { report.stationID = it }
                    .onFailure { return ValidationError.InputError("station-id must be an Int, is: $param").left() }
            }
            params["partner-id"]?.let { param ->
                kotlin.runCatching { param.toInt() }
                    .onSuccess { report.partnerID = it }
                    .onFailure { return ValidationError.InputError("partner-id must be an Int, is: $param").left() }
            }
            params["from-date"]?.let { param ->
                kotlin.runCatching { LocalDateTime.parse(param, DateTimeFormatter.ISO_DATE_TIME) }
                    .onFailure {
                        return ValidationError.InputError("Invalid from-date: must be a ISO-compliant string, is: $param")
                            .left()
                    }
                    .onSuccess { report.fromDate = it }
            }
            params["to-date"]?.let { param ->
                kotlin.runCatching { LocalDateTime.parse(param, DateTimeFormatter.ISO_DATE_TIME) }
                    .onFailure {
                        return ValidationError.InputError("Invalid to-date: must be a ISO-compliant string, is: $param")
                            .left()
                    }
                    .onSuccess { report.toDate = it }
            }
            return report.right()
        }
    }
}