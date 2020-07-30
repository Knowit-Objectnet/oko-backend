package ombruk.backend.calendar.form

import arrow.core.Either
import io.ktor.locations.Location
import ombruk.backend.shared.error.ValidationError
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.isGreaterThanStartDateTime
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isNotNull
import org.valiktor.functions.isNull
import org.valiktor.validate
import java.time.LocalDateTime

@Location("/events")
data class EventDeleteForm(
    var eventId: Int? = null,
    var recurrenceRuleId: Int? = null,
    var fromDate: LocalDateTime? = null,
    var toDate: LocalDateTime? = null
) : IForm<EventDeleteForm> {
    override fun validOrError(): Either<ValidationError, EventDeleteForm> = runCatchingValidation {
        validate(this) {
            if (eventId == null) validate(EventDeleteForm::recurrenceRuleId).isNotNull()
            else validate(EventDeleteForm::recurrenceRuleId).isNull()

            validate(EventDeleteForm::toDate).isGreaterThanStartDateTime(fromDate)
        }
    }
}