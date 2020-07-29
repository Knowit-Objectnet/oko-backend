package calendar.form

import arrow.core.Either
import io.ktor.locations.Location
import kotlinx.serialization.Serializable
import ombruk.backend.shared.error.ValidationError
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.model.LocalDateTimeSerializer
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThan
import org.valiktor.functions.isNull
import org.valiktor.validate
import java.time.LocalDateTime


@Serializable
@Location("/events/")
data class EventGetForm(
    val eventID: Int? = null,
    val stationID: Int? = null,
    val partnerID: Int? = null,
    val recurrenceRuleID: Int? = null,
    @Serializable(with = LocalDateTimeSerializer::class) val fromDate: LocalDateTime? = null,
    @Serializable(with = LocalDateTimeSerializer::class) val toDate: LocalDateTime? = null
) : IForm<EventGetForm> {

    override fun validOrError(): Either<ValidationError, EventGetForm> = runCatchingValidation {
        validate(this) {
            validate(EventGetForm::eventID).isGreaterThan(0)
            if (fromDate != null && toDate != null) validate(EventGetForm::fromDate).isGreaterThan(toDate)

            if (eventID != null){
                validate(EventGetForm::stationID).isNull()
                validate(EventGetForm::partnerID).isNull()
                validate(EventGetForm::recurrenceRuleID).isNull()
                validate(EventGetForm::fromDate).isNull()
                validate(EventGetForm::toDate).isNull()
            }

        }
    }
}

