package no.oslokommune.ombruk.uttak.form

import arrow.core.Either
import io.swagger.v3.oas.annotations.media.Schema
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
    @field:Schema(required = true, example = "1", nullable = false) val stasjonId: Int,
    @field:Schema(
        required = false,
        example = "1",
        nullable = false,
        description = "Posting an Uttak with a partnerId means that the Uttak is not assigned to a Partner yet."
    ) val partnerId: Int? = null, // Optional partner. An uttak without a partner is arranged by the stasjon only.
    @field:Schema(
        required = true,
        example = "1",
        nullable = false,
        description = "By omitting gjentakelsesRegel, you specify that the Uttak will only occur once. By including it, you define at what times the Uttak should occur",
        implementation = GjentakelsesRegel::class
    ) val gjentakelsesRegel: GjentakelsesRegel? = null,
    @field:Schema(
        required = true,
        defaultValue = "GJENTAKENDE",
        example = "ENKELT",
        description = "Denotes what kind of Uttak should be created",
        implementation = UttaksType::class
    )val type: UttaksType = UttaksType.GJENTAKENDE,
    @get:Schema(
        required = true,
        description = "The date of the first (or only) Uttak"
    ) @Serializable(with = LocalDateTimeSerializer::class) val startTidspunkt: LocalDateTime,
    @field:Schema(
        required = true,
        description = "The date of the final Uttak. Must be after startTidspunkt. If the Uttak only occurs once," +
                "the date of sluttTidspunkt must correspond with startTidspunkt"
    )
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
    validate(GjentakelsesRegel::until).isGreaterThanStartDateTime(startDateTime)

    if (antall == null) validate(GjentakelsesRegel::until).isNotNull()
    if (until == null) validate(GjentakelsesRegel::antall).isNotNull()
}

