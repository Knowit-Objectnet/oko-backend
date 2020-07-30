package ombruk.backend.calendar.form

import arrow.core.Either
import kotlinx.serialization.Serializable
import ombruk.backend.calendar.database.EventRepository
import ombruk.backend.shared.error.ValidationError
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.model.serializer.LocalDateTimeSerializer
import ombruk.backend.shared.utils.validation.isGreaterThanStartDateTime
import ombruk.backend.shared.utils.validation.isLessThanEndDateTime
import ombruk.backend.shared.utils.validation.runCatchingValidation
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
                validate(EventUpdateForm::startDateTime).isGreaterThanStartDateTime(newStartDateTime)
            }
        }
    }
}





