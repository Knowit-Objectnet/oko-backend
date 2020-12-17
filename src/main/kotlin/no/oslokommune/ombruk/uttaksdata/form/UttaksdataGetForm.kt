package no.oslokommune.ombruk.uttaksdata.form

import io.ktor.locations.Location
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Schema
import no.oslokommune.ombruk.shared.form.IForm
import no.oslokommune.ombruk.shared.utils.validation.isLessThanEndDateTime
import no.oslokommune.ombruk.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThan
import org.valiktor.validate
import java.time.LocalDateTime

@Location("/")
data class UttaksdataGetForm(
    @get:Parameter(
        `in` = ParameterIn.QUERY,
        name = "uttakId",
        schema = Schema(type = "int32"),
        example = "1",
        description = "The ID of the Uttaksdata to get"
    ) val uttakId: Int? = null,
    @get:Parameter(
        `in` = ParameterIn.QUERY,
        name = "minVekt",
        schema = Schema(type = "int32"),
        example = "10",
        description = "The minimal amount of weight in kilos an Uttaksdata must have to be included"
    ) val minVekt: Int? = null,
    @get:Parameter(
        `in` = ParameterIn.QUERY,
        name = "partnerId",
        schema = Schema(type = "int32"),
        example = "15",
        description = "Only show UttaksData related to this partner"
    ) val partnerId: Int? = null,
    @get:Parameter(
        `in` = ParameterIn.QUERY,
        name = "stasjonId",
        schema = Schema(type = "int32"),
        example = "10",
        description = "Only show UttaksData that is associated with this Stasjon ID"
    ) val stasjonId: Int? = null,
    @get:Parameter(
        `in` = ParameterIn.QUERY,
        name = "maxVekt",
        schema = Schema(type = "int32"),
        example = "100",
        description = "The maximum amount of weight in kilos an Uttaksdata can have to be included"
    ) val maxVekt: Int? = null,
    @get:Parameter(
        `in` = ParameterIn.QUERY,
        name = "fraRapportertTidspunkt",
        schema = Schema(type = "string", format = "date-time"),
        example = "2020-10-05T14:50:00Z",
        description = "Only include Uttaksdata from this date. Inclusive"
    ) val fraRapportertTidspunkt: LocalDateTime? = null,
    @get:Parameter(
        `in` = ParameterIn.QUERY,
        name = "tilRapportertTidspunkt",
        schema = Schema(type = "string", format = "date-time"),
        example = "2020-11-05T14:50:00Z",
        description = "Only include Uttaksdata up until this date. Inclusive"
    ) val tilRapportertTidspunkt: LocalDateTime? = null
) : IForm<UttaksdataGetForm> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(UttaksdataGetForm::uttakId).isGreaterThan(0)

            validate(UttaksdataGetForm::minVekt).isGreaterThan(0)
            validate(UttaksdataGetForm::maxVekt).isGreaterThan(0)
            if (minVekt != null && maxVekt !== null) validate(UttaksdataGetForm::maxVekt).isGreaterThan(minVekt)

            if (fraRapportertTidspunkt != null && tilRapportertTidspunkt != null)
                validate(UttaksdataGetForm::fraRapportertTidspunkt).isLessThanEndDateTime(tilRapportertTidspunkt)
        }
    }
}