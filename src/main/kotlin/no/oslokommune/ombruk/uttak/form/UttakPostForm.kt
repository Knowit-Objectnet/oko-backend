package no.oslokommune.ombruk.uttak.form

import arrow.core.Either
import kotlinx.serialization.Serializable
import no.oslokommune.ombruk.stasjon.database.StasjonRepository
import no.oslokommune.ombruk.uttak.model.RecurrenceRule
import no.oslokommune.ombruk.uttak.utils.CreateUttakFormIterator
import no.oslokommune.ombruk.uttak.utils.NonRecurringCreateUttakFormIterator
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
data class UttakPostForm(
    @Serializable(with = LocalDateTimeSerializer::class) var startDateTime: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class) var endDateTime: LocalDateTime,
    val stasjonId: Int,
    val partnerId: Int? = null, // Optional partner. An uttak without a partner is arranged by the stasjon only.
    var recurrenceRule: RecurrenceRule? = null
) : Iterable<UttakPostForm>, IForm<UttakPostForm> {
    override fun iterator() = when (recurrenceRule) {
        null -> NonRecurringCreateUttakFormIterator(this)
        else -> CreateUttakFormIterator(this)
    }

    override fun validOrError(): Either<ValidationError, UttakPostForm> = runCatchingValidation {
        validate(this) {
            validate(UttakPostForm::endDateTime).isGreaterThanStartDateTime(startDateTime)
            validate(UttakPostForm::endDateTime).isSameDateAs(startDateTime)

            validate(UttakPostForm::startDateTime).isWithinOpeningHoursOf(it.stasjonId)
            validate(UttakPostForm::endDateTime).isWithinOpeningHoursOf(it.stasjonId)

            validate(UttakPostForm::stasjonId).isPositive()

            validate(UttakPostForm::stasjonId).isInRepository(StasjonRepository)
            validate(UttakPostForm::partnerId).isInRepository(PartnerRepository)
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

