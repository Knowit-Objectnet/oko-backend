package ombruk.backend.calendar.form

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import kotlinx.serialization.Serializable
import ombruk.backend.calendar.database.StationRepository
import ombruk.backend.calendar.model.RecurrenceRule
import ombruk.backend.calendar.utils.CreateEventFormIterator
import ombruk.backend.calendar.utils.NonRecurringCreateEventFormIterator
import ombruk.backend.partner.database.PartnerRepository
import ombruk.backend.shared.error.ValidationError
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.model.LocalDateTimeSerializer
import ombruk.backend.shared.utils.validators.isInRepository
import org.valiktor.ConstraintViolationException
import org.valiktor.functions.isGreaterThan
import org.valiktor.functions.isGreaterThanOrEqualTo
import org.valiktor.functions.validate
import org.valiktor.validate
import java.time.LocalDateTime

@Serializable
data class EventPostForm(
    @Serializable(with = LocalDateTimeSerializer::class) var startDateTime: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class) var endDateTime: LocalDateTime,
    val stationId: Int,
    val partnerId: Int,
    var recurrenceRule: RecurrenceRule? = null
) : Iterable<EventPostForm>, IForm<EventPostForm> {
    override fun iterator() = when (recurrenceRule) {
        null -> NonRecurringCreateEventFormIterator(this)
        else -> CreateEventFormIterator(this)
    }

    override fun validOrError(): Either<ValidationError, EventPostForm> {
        return try {
            validate(this) {
                validate(EventPostForm::endDateTime).isGreaterThan(startDateTime)
                validate(EventPostForm::stationId).isInRepository(StationRepository)
                validate(EventPostForm::partnerId).isInRepository(PartnerRepository)

                recurrenceRule?.let {
                    validate(EventPostForm::recurrenceRule).validate {
                        validate(RecurrenceRule::interval).isGreaterThanOrEqualTo(1)
                        it.until?.let{ validate(RecurrenceRule::until).isGreaterThan(startDateTime) }
                        it.count?.let { validate(RecurrenceRule::count).isGreaterThanOrEqualTo(1) }
                    }
                }

            }
            this.right()
        } catch (e: ConstraintViolationException) {
            val msg = e.constraintViolations.joinToString { "${it.property} is invalid (${it.constraint})" }
            ValidationError.Unprocessable(msg).left()
        }
    }
}
