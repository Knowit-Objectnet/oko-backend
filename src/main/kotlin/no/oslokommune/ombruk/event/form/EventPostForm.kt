package no.oslokommune.ombruk.event.form

import arrow.core.Either
import kotlinx.serialization.Serializable
import no.oslokommune.ombruk.station.database.StationRepository
import no.oslokommune.ombruk.event.model.RecurrenceRule
import no.oslokommune.ombruk.event.utils.CreateEventFormIterator
import no.oslokommune.ombruk.event.utils.NonRecurringCreateEventFormIterator
import no.oslokommune.ombruk.partner.database.PartnerRepository
import no.oslokommune.ombruk.shared.error.ValidationError
import no.oslokommune.ombruk.shared.form.IForm
import no.oslokommune.ombruk.shared.model.serializer.LocalDateTimeSerializer
import no.oslokommune.ombruk.shared.utils.validation.*
import org.valiktor.functions.isGreaterThanOrEqualTo
import org.valiktor.functions.isNotNull
import org.valiktor.functions.isPositive
import org.valiktor.validate
import java.time.LocalDateTime

@Serializable
data class EventPostForm(
    @Serializable(with = LocalDateTimeSerializer::class) var startDateTime: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class) var endDateTime: LocalDateTime,
    val stationId: Int,
    val partnerId: Int? = null, // Optional partner. An event without a partner is arranged by the station only.
    var recurrenceRule: RecurrenceRule? = null
) : Iterable<EventPostForm>, IForm<EventPostForm> {
    override fun iterator() = when (recurrenceRule) {
        null -> NonRecurringCreateEventFormIterator(this)
        else -> CreateEventFormIterator(this)
    }

    override fun validOrError(): Either<ValidationError, EventPostForm> = runCatchingValidation {
        validate(this) {
            validate(EventPostForm::endDateTime).isGreaterThanStartDateTime(startDateTime)
            validate(EventPostForm::endDateTime).isSameDateAs(startDateTime)

            validate(EventPostForm::startDateTime).isWithinOpeningHoursOf(it.stationId)
            validate(EventPostForm::endDateTime).isWithinOpeningHoursOf(it.stationId)

            validate(EventPostForm::stationId).isPositive()

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

