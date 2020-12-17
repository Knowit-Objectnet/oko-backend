package no.oslokommune.ombruk.uttak.form

import arrow.core.Either
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Schema
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
    @get:Parameter(
        `in` = ParameterIn.QUERY,
        name = "id",
        schema = Schema(type = "int32"),
        description = "Id of Uttak to get"
    ) val id: Int? = null,
    @get:Parameter(
        `in` = ParameterIn.QUERY,
        name = "stasjonId",
        schema = Schema(type = "int32"),
        description = "Filter Uttak by Stasjon ID"
    ) val stasjonId: Int? = null,
    @get:Parameter(
        `in` = ParameterIn.QUERY,
        name = "partnerId",
        schema = Schema(type = "int32"),
        description = "Filter Uttak by partner ID"
    ) val partnerId: Int? = null,
    @get:Parameter(
        `in` = ParameterIn.QUERY,
        name = "beskrivelse",
        schema = Schema(type = "string"),
        description = "Filter Uttak by description"
    )val beskrivelse: String? = null,
    @get:Parameter(
        `in` = ParameterIn.QUERY,
        name = "gjentakelsesRegelID",
        schema = Schema(type = "int32"),
        description = "Filter Uttak by gjentakelsesRegel"
    )val gjentakelsesRegelID: Int? = null,
    val type: String? = null,
    @get:Parameter(
        `in` = ParameterIn.QUERY,
        name = "startTidspunkt",
        schema = Schema(type = "string", format = "date-time"),
        description = "Only shows Uttak that occur after this date. Inclusive."
    ) @Serializable(with = LocalDateTimeSerializer::class) val startTidspunkt: LocalDateTime? = null,
    @get:Parameter(
        `in` = ParameterIn.QUERY,
        name = "sluttTidspunkt",
        schema = Schema(type = "string", format = "date-time"),
        description = "Only shows Uttak that occur before this date. Inclusive."
    )@Serializable(with = LocalDateTimeSerializer::class) val sluttTidspunkt: LocalDateTime? = null
) : IForm<UttakGetForm> {

    override fun validOrError(): Either<ValidationError, UttakGetForm> = runCatchingValidation {
        validate(this) {
            validate(UttakGetForm::id).isGreaterThan(0)
            validate(UttakGetForm::stasjonId).isGreaterThan(0)
            validate(UttakGetForm::partnerId).isGreaterThan(0)
            validate(UttakGetForm::gjentakelsesRegelID).isGreaterThan(0)

            // TODO: Before and after
            if (startTidspunkt != null && sluttTidspunkt != null) validate(UttakGetForm::startTidspunkt).isLessThanEndDateTime(
                sluttTidspunkt
            )
        }
    }
}

