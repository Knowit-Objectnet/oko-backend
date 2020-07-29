package ombruk.backend.calendar.form

import arrow.core.Either
import kotlinx.serialization.Serializable
import ombruk.backend.calendar.database.StationRepository
import ombruk.backend.calendar.model.RecurrenceRule
import ombruk.backend.calendar.utils.CreateEventFormIterator
import ombruk.backend.calendar.utils.NonRecurringCreateEventFormIterator
import ombruk.backend.partner.database.PartnerRepository
import ombruk.backend.shared.error.ValidationError
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.model.LocalDateTimeSerializer
import ombruk.backend.shared.utils.validation.isGreaterThanStartDateTime
import ombruk.backend.shared.utils.validation.isInRepository
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThanOrEqualTo
import org.valiktor.functions.isNotNull
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

    override fun validOrError(): Either<ValidationError, EventPostForm> = runCatchingValidation {
        validate(this) {
            validate(EventPostForm::endDateTime).isGreaterThanStartDateTime(startDateTime)
            validate(EventPostForm::stationId).isInRepository(StationRepository)
            validate(EventPostForm::partnerId).isInRepository(PartnerRepository)
            recurrenceRule?.validateSelf(startDateTime)
        }
    }
}


private fun RecurrenceRule.validateSelf(startDateTime: LocalDateTime) = validate(this) {

    validate(RecurrenceRule::interval).isGreaterThanOrEqualTo(1)
    validate(RecurrenceRule::count).isGreaterThanOrEqualTo(1)
    validate(RecurrenceRule::until).isGreaterThanStartDateTime(startDateTime)

    if (count == null) validate(RecurrenceRule::until).isNotNull()
    if (until == null) validate(RecurrenceRule::count).isNotNull()
}