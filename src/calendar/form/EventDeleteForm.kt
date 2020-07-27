package ombruk.backend.calendar.form

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.ktor.http.Parameters
import ombruk.backend.shared.error.ValidationError
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class EventDeleteForm private constructor(
    var eventID: Int? = null,
    var recurrenceRuleID: Int? = null,
    var fromDate: LocalDateTime? = null,
    var toDate: LocalDateTime? = null
) {
    companion object {
        fun create(params: Parameters): Either<ValidationError, EventDeleteForm> {
            val event = EventDeleteForm()
            params["event-id"]?.let { param ->
                kotlin.runCatching { param.toInt() }
                    .onSuccess { event.eventID = it }
                    .onFailure { return ValidationError.InputError("event-id must be an Int, is: $param")
                        .left() }
            }
            params["recurrence-rule-id"]?.let { param ->
                kotlin.runCatching { param.toInt() }
                    .onSuccess { event.recurrenceRuleID = it }
                    .onFailure {
                        return ValidationError.InputError("recurrence-rule-id must be an Int, is: $param")
                            .left()
                    }
            }
            if (event.eventID != null && event.recurrenceRuleID != null) return ValidationError.InvalidStateError(
                "event-id and recurrence-rule-id cannot both be set"
            )
                .left()
            if (event.eventID == null && event.recurrenceRuleID == null) return ValidationError.InvalidStateError(
                "Must pass in event-id or recurrence-rule-id"
            )
                .left()
            params["from-date"]?.let { param ->
                kotlin.runCatching { LocalDateTime.parse(param, DateTimeFormatter.ISO_DATE_TIME) }
                    .onFailure {
                        return ValidationError.InputError("Invalid from-date: must be a ISO-compliant string, is: $param")
                            .left()
                    }
                    .onSuccess { event.fromDate = it }
            }
            params["to-date"]?.let { param ->
                kotlin.runCatching { LocalDateTime.parse(param, DateTimeFormatter.ISO_DATE_TIME) }
                    .onFailure {
                        return ValidationError.InputError("Invalid to-date: must be a ISO-compliant string, is: $param")
                            .left()
                    }
                    .onSuccess { event.toDate = it }
            }
            return event.right()
        }
    }
}