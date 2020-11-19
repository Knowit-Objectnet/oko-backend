package no.oslokommune.ombruk.uttak.form

import arrow.core.Either
import kotlinx.serialization.Serializable
import no.oslokommune.ombruk.stasjon.database.StasjonRepository
import no.oslokommune.ombruk.uttak.model.GjentakelsesRegel
import no.oslokommune.ombruk.uttak.utils.PostUttakFormIterator
import no.oslokommune.ombruk.uttak.utils.NonRecurringCreateUttakFormIterator
import no.oslokommune.ombruk.partner.database.PartnerRepository
import no.oslokommune.ombruk.shared.error.ValidationError
import no.oslokommune.ombruk.shared.form.IForm
import no.oslokommune.ombruk.shared.model.serializer.LocalDateTimeSerializer
import no.oslokommune.ombruk.shared.utils.validation.*
import no.oslokommune.ombruk.uttak.model.UttaksType
import org.valiktor.functions.isGreaterThanOrEqualTo
import org.valiktor.functions.isNotNull
import org.valiktor.functions.isPositive
import org.valiktor.validate
import java.time.LocalDateTime

@Serializable
data class UttakPostForm(
    val stasjonId: Int,
    val partnerId: Int? = null, // Optional partner. An uttak without a partner is arranged by the stasjon only.
    val gjentakelsesRegel: GjentakelsesRegel? = null,
    val type: UttaksType = UttaksType.GJENTAKENDE,
    @Serializable(with = LocalDateTimeSerializer::class) val startTidspunkt: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class) val sluttTidspunkt: LocalDateTime
) : Iterable<UttakPostForm>, IForm<UttakPostForm> {
    override fun iterator() = when (gjentakelsesRegel) {
        null -> NonRecurringCreateUttakFormIterator(this)
        else -> PostUttakFormIterator(this)
    }

    override fun validOrError(): Either<ValidationError, UttakPostForm> = runCatchingValidation {
        validate(this) {
            validate(UttakPostForm::sluttTidspunkt).isGreaterThanStartDateTime(startTidspunkt)
            validate(UttakPostForm::sluttTidspunkt).isSameDateAs(startTidspunkt)

            validate(UttakPostForm::startTidspunkt).isWithinOpeningHoursOf(it.stasjonId)
            validate(UttakPostForm::sluttTidspunkt).isWithinOpeningHoursOf(it.stasjonId)

            validate(UttakPostForm::stasjonId).isPositive()

            validate(UttakPostForm::stasjonId).isInRepository(StasjonRepository)
            validate(UttakPostForm::partnerId).isInRepository(PartnerRepository)
            gjentakelsesRegel?.validateSelf(startTidspunkt)
        }
    }
}


private fun GjentakelsesRegel.validateSelf(startDateTime: LocalDateTime) = validate(this) {

    validate(GjentakelsesRegel::intervall).isGreaterThanOrEqualTo(1)
    validate(GjentakelsesRegel::antall).isGreaterThanOrEqualTo(1)
    validate(GjentakelsesRegel::sluttTidspunkt).isGreaterThanStartDateTime(startDateTime)

    if (antall == null) validate(GjentakelsesRegel::sluttTidspunkt).isNotNull()
    if (sluttTidspunkt == null) validate(GjentakelsesRegel::antall).isNotNull()
}

