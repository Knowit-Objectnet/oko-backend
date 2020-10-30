package no.oslokommune.ombruk.uttak.form

import arrow.core.Either
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import kotlinx.serialization.Serializable
import no.oslokommune.ombruk.shared.error.ValidationError
import no.oslokommune.ombruk.shared.form.IForm
import no.oslokommune.ombruk.shared.model.serializer.LocalDateTimeSerializer
import no.oslokommune.ombruk.shared.utils.validation.isLessThanEndDateTime
import no.oslokommune.ombruk.shared.utils.validation.runCatchingValidation
import no.oslokommune.ombruk.uttak.model.UttaksType
import org.valiktor.functions.isGreaterThan
import org.valiktor.functions.isNull
import org.valiktor.validate
import java.time.LocalDateTime


@KtorExperimentalLocationsAPI
@Location("/")
data class UttakGetForm(
        val id: Int? = null,
        val stasjonID: Int? = null,
        val partnerID: Int? = null,
        val beskrivelse: String? = null,
        val gjentakelsesRegelID: Int? = null,
        val type: UttaksType? = null,
        @Serializable(with = LocalDateTimeSerializer::class) val startTidspunkt: LocalDateTime? = null,
        @Serializable(with = LocalDateTimeSerializer::class) val sluttTidspunkt: LocalDateTime? = null
) : IForm<UttakGetForm> {

    override fun validOrError(): Either<ValidationError, UttakGetForm> = runCatchingValidation {
        validate(this) {
            validate(UttakGetForm::id).isGreaterThan(0)
            validate(UttakGetForm::stasjonID).isGreaterThan(0)
            validate(UttakGetForm::partnerID).isGreaterThan(0)
            validate(UttakGetForm::gjentakelsesRegelID).isGreaterThan(0)

            if (startTidspunkt != null && sluttTidspunkt != null) validate(UttakGetForm::startTidspunkt).isLessThanEndDateTime(sluttTidspunkt)

            // TODO: This might not make sense with the new model
            if (id != null) {
                validate(UttakGetForm::stasjonID).isNull()
                validate(UttakGetForm::partnerID).isNull()
                validate(UttakGetForm::gjentakelsesRegelID).isNull()
                validate(UttakGetForm::startTidspunkt).isNull()
                validate(UttakGetForm::sluttTidspunkt).isNull()
            }

        }
    }
}

