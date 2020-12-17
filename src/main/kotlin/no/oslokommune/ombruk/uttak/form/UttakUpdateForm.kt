package no.oslokommune.ombruk.uttak.form

import arrow.core.Either
import io.swagger.v3.oas.annotations.media.Schema
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
import no.oslokommune.ombruk.uttak.model.UttaksType
import org.valiktor.functions.isGreaterThan
import org.valiktor.functions.isNotNull
import org.valiktor.functions.isNull
import org.valiktor.functions.isValid
import org.valiktor.validate
import java.time.DayOfWeek
import java.time.LocalDateTime

@Serializable
data class UttakUpdateForm(
    @field:Schema(description = "The ID of the Uttak to be edited", required = true) val id: Int,
    @field:Schema(
        description = "The type of the Uttak",
        implementation = UttaksType::class
    ) val type: String? = null, // Uttakstype. WHy would you allow the the client to alter the type? Should the server not handle this?
//    @field:Schema(description = "Changes the station that a specific Uttak belongs to") val stasjonId: Int? = null,
//    @field:Schema(description = "Changes the partner that a specific Uttak belongs to") val partnerId: Int? = null,
//    @field:Schema(description = "Changes the description of an Uttak") val beskrivelse: String? = null,
//    @field:Schema(
//        description = "Alters the GjentakelsesRegel of an Uttak"
//    ) val gjentakelsesRegel: GjentakelsesRegelUpdate? = null, // Why would you need to update a GjentakelsesRegel?
    @field:Schema(
        description = "Alters the start date time of an Uttak"
    ) @Serializable(with = LocalDateTimeSerializer::class) val startTidspunkt: LocalDateTime? = null,
    @field:Schema(
        description = "Alters the end date time of an Uttak"
    ) @Serializable(with = LocalDateTimeSerializer::class) val sluttTidspunkt: LocalDateTime? = null
) : IForm<UttakUpdateForm> {
    override fun validOrError(): Either<ValidationError, UttakUpdateForm> = runCatchingValidation {
        validate(this) {
            validate(UttakUpdateForm::id).isGreaterThan(0)
            if (startTidspunkt != null) validate(UttakUpdateForm::sluttTidspunkt).isNotNull()
            if (sluttTidspunkt != null) validate(UttakUpdateForm::startTidspunkt).isNotNull()

            if (type != null) {
                validate(UttakUpdateForm::type).isValid {
                    UttaksType.values().any { it.name == type.toUpperCase() }
                }
            }

            UttakRepository.getUttakByID(id).map { uttak ->
                val newStartDateTime = startTidspunkt ?: uttak.startTidspunkt
                val newEndDateTime = sluttTidspunkt ?: uttak.sluttTidspunkt
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




