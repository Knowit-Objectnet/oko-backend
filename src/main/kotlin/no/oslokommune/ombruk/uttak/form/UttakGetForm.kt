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
import org.valiktor.functions.isGreaterThan
import org.valiktor.validate
import java.time.LocalDateTime


@KtorExperimentalLocationsAPI
@Location("/")
@Serializable
data class UttakGetForm(
    val id: Int? = null,
    val stasjonId: Int? = null,
    val partnerId: Int? = null,
    val beskrivelse: String? = null,
    val gjentakelsesRegelID: Int? = null,
    val type: String? = null,
    @Serializable(with = LocalDateTimeSerializer::class) val startTidspunkt: LocalDateTime? = null,
    @Serializable(with = LocalDateTimeSerializer::class) val sluttTidspunkt: LocalDateTime? = null
) : IForm<UttakGetForm> {

    override fun validOrError(): Either<ValidationError, UttakGetForm> = runCatchingValidation {
        validate(this) {
            validate(UttakGetForm::id).isGreaterThan(0)
            validate(UttakGetForm::stasjonId).isGreaterThan(0)
            validate(UttakGetForm::partnerId).isGreaterThan(0)
            validate(UttakGetForm::gjentakelsesRegelID).isGreaterThan(0)

            // TODO: Before and after
            if (startTidspunkt != null && sluttTidspunkt != null) validate(UttakGetForm::startTidspunkt).isLessThanEndDateTime(sluttTidspunkt)
        }
    }
}

