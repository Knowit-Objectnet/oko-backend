package no.oslokommune.ombruk.event.form

import arrow.core.Either
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import kotlinx.serialization.Serializable
import no.oslokommune.ombruk.shared.error.ValidationError
import no.oslokommune.ombruk.shared.form.IForm
import no.oslokommune.ombruk.shared.model.serializer.LocalDateTimeSerializer
import no.oslokommune.ombruk.shared.utils.validation.isLessThanEndDateTime
import no.oslokommune.ombruk.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThan
import org.valiktor.functions.isNull
import org.valiktor.validate
import java.time.LocalDateTime


@KtorExperimentalLocationsAPI
@Location("/")
data class EventGetForm(
    val eventId: Int? = null,
    val stationId: Int? = null,
    val partnerId: Int? = null,
    val recurrenceRuleId: Int? = null,
    @Serializable(with = LocalDateTimeSerializer::class) val fromDate: LocalDateTime? = null,
    @Serializable(with = LocalDateTimeSerializer::class) val toDate: LocalDateTime? = null
) : IForm<EventGetForm> {

    override fun validOrError(): Either<ValidationError, EventGetForm> = runCatchingValidation {
        validate(this) {
            validate(EventGetForm::eventId).isGreaterThan(0)
            validate(EventGetForm::stationId).isGreaterThan(0)
            validate(EventGetForm::partnerId).isGreaterThan(0)
            validate(EventGetForm::recurrenceRuleId).isGreaterThan(0)

            if (fromDate != null && toDate != null) validate(EventGetForm::fromDate).isLessThanEndDateTime(toDate)

            if (eventId != null) {
                validate(EventGetForm::stationId).isNull()
                validate(EventGetForm::partnerId).isNull()
                validate(EventGetForm::recurrenceRuleId).isNull()
                validate(EventGetForm::fromDate).isNull()
                validate(EventGetForm::toDate).isNull()
            }

        }
    }
}

