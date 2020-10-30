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
import no.oslokommune.ombruk.uttak.model.GjentakelsesRegel
import no.oslokommune.ombruk.uttak.model.UttaksType
import org.valiktor.functions.isGreaterThan
import org.valiktor.functions.isNotNull
import org.valiktor.validate
import java.time.DayOfWeek
import java.time.LocalDateTime

@Serializable
data class UttakUpdateForm(
        val id: Int,
        val type: UttaksType? = null,
        val stasjonID: Int? = null,
        val samarbeidspartnerID: Int? = null,
        val beskrivelse: Int? = null,
        val gjentakelsesRegel: GjentakelsesRegelUpdate? = null,
        @Serializable(with = LocalDateTimeSerializer::class) val startTidspunkt: LocalDateTime? = null,
        @Serializable(with = LocalDateTimeSerializer::class) val sluttTidspunkt: LocalDateTime? = null
) : IForm<UttakUpdateForm> {
    override fun validOrError(): Either<ValidationError, UttakUpdateForm> = runCatchingValidation {
        validate(this) {
            validate(UttakUpdateForm::id).isGreaterThan(0)
            if (startTidspunkt == null) validate(UttakUpdateForm::sluttTidspunkt).isNotNull()
            if (sluttTidspunkt == null) validate(UttakUpdateForm::startTidspunkt).isNotNull()

            UttakRepository.getUttakByID(id).map { uttak ->
                val newStartDateTime = startTidspunkt ?: uttak.startDateTime
                val newEndDateTime = sluttTidspunkt ?: uttak.endDateTime
                validate(UttakUpdateForm::startTidspunkt).isLessThanEndDateTime(newEndDateTime)
                validate(UttakUpdateForm::sluttTidspunkt).isSameDateAs(startTidspunkt)
                validate(UttakUpdateForm::sluttTidspunkt).isGreaterThanStartDateTime(newStartDateTime)

                validate(UttakUpdateForm::startTidspunkt).isWithinOpeningHoursOf(uttak.stasjon.id)
                validate(UttakUpdateForm::sluttTidspunkt).isWithinOpeningHoursOf(uttak.stasjon.id)
            }
        }
    }
}

@Serializable
data class GjentakelsesRegelUpdate(
        var id: Int,
        val intervall: Int? = null,
        val antall: Int? = null,
        @Serializable
        val dager: List<DayOfWeek>? = null,
        @Serializable(with = LocalDateTimeSerializer::class)
        val sluttTidspunkt: LocalDateTime? = null
)




