package no.oslokommune.ombruk.uttak.form

import arrow.core.Either
import kotlinx.serialization.Serializable
import no.oslokommune.ombruk.uttak.database.UttakRepository
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
data class UttakUpdateForm(
    val id: Int,
    @Serializable(with = LocalDateTimeSerializer::class) val startDateTime: LocalDateTime? = null,
    @Serializable(with = LocalDateTimeSerializer::class) val endDateTime: LocalDateTime? = null
) : IForm<UttakUpdateForm> {
    override fun validOrError(): Either<ValidationError, UttakUpdateForm> = runCatchingValidation {
        validate(this) {
            validate(UttakUpdateForm::id).isGreaterThan(0)
            if (startDateTime == null) validate(UttakUpdateForm::endDateTime).isNotNull()
            if (endDateTime == null) validate(UttakUpdateForm::startDateTime).isNotNull()

            UttakRepository.getUttakByID(id).map { uttak ->
                val newStartDateTime = startDateTime ?: uttak.startDateTime
                val newEndDateTime = endDateTime ?: uttak.endDateTime
                validate(UttakUpdateForm::startDateTime).isLessThanEndDateTime(newEndDateTime)
                validate(UttakUpdateForm::endDateTime).isSameDateAs(startDateTime)
                validate(UttakUpdateForm::endDateTime).isGreaterThanStartDateTime(newStartDateTime)

                validate(UttakUpdateForm::startDateTime).isWithinOpeningHoursOf(uttak.stasjon.id)
                validate(UttakUpdateForm::endDateTime).isWithinOpeningHoursOf(uttak.stasjon.id)
            }
        }
    }
}





