package ombruk.backend.calendar.form.event

import arrow.core.Either
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import ombruk.backend.shared.error.ValidationError
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.isGreaterThanStartDateTime
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThan
import org.valiktor.validate
import java.time.LocalDateTime

@KtorExperimentalLocationsAPI
@Location("/")
data class EventDeleteForm(
    var eventId: Int? = null,
    var recurrenceRuleId: Int? = null,
    var fromDate: LocalDateTime? = null,
    var toDate: LocalDateTime? = null,
    var stationId: Int? = null,
    var partnerId: Int? = null
) : IForm<EventDeleteForm> {
    override fun validOrError(): Either<ValidationError, EventDeleteForm> = runCatchingValidation {
        validate(this) {

            validate(EventDeleteForm::eventId).isGreaterThan(0)
            validate(EventDeleteForm::recurrenceRuleId).isGreaterThan(0)
            validate(EventDeleteForm::stationId).isGreaterThan(0)
            validate(EventDeleteForm::partnerId).isGreaterThan(0)

            validate(EventDeleteForm::toDate).isGreaterThanStartDateTime(fromDate)
        }
    }
}