package no.oslokommune.ombruk.event.form

import arrow.core.Either
import kotlinx.serialization.Serializable
import no.oslokommune.ombruk.event.database.EventRepository
import no.oslokommune.ombruk.shared.error.ValidationError
import no.oslokommune.ombruk.shared.form.IForm
import no.oslokommune.ombruk.shared.model.serializer.LocalDateTimeSerializer
import no.oslokommune.ombruk.shared.utils.validation.isGreaterThanStartDateTime
import no.oslokommune.ombruk.shared.utils.validation.isLessThanEndDateTime
import no.oslokommune.ombruk.shared.utils.validation.isSameDateAs
import no.oslokommune.ombruk.shared.utils.validation.isWithinOpeningHoursOf
import no.oslokommune.ombruk.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThan
import org.valiktor.functions.isNotNull
import org.valiktor.validate
import java.time.LocalDateTime

@Serializable
data class EventUpdateForm(
    val id: Int,
    @Serializable(with = LocalDateTimeSerializer::class) val startDateTime: LocalDateTime? = null,
    @Serializable(with = LocalDateTimeSerializer::class) val endDateTime: LocalDateTime? = null
) : IForm<EventUpdateForm> {
    override fun validOrError(): Either<ValidationError, EventUpdateForm> = runCatchingValidation {
        validate(this) {
            validate(EventUpdateForm::id).isGreaterThan(0)
            if (startDateTime == null) validate(EventUpdateForm::endDateTime).isNotNull()
            if (endDateTime == null) validate(EventUpdateForm::startDateTime).isNotNull()

            EventRepository.getEventByID(id).map { event ->
                val newStartDateTime = startDateTime ?: event.startDateTime
                val newEndDateTime = endDateTime ?: event.endDateTime
                validate(EventUpdateForm::startDateTime).isLessThanEndDateTime(newEndDateTime)
                validate(EventUpdateForm::endDateTime).isSameDateAs(startDateTime)
                validate(EventUpdateForm::endDateTime).isGreaterThanStartDateTime(newStartDateTime)

                validate(EventUpdateForm::startDateTime).isWithinOpeningHoursOf(event.station.id)
                validate(EventUpdateForm::endDateTime).isWithinOpeningHoursOf(event.station.id)
            }
        }
    }
}





