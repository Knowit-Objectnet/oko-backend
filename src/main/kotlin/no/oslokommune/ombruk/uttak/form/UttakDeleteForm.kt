package no.oslokommune.ombruk.uttak.form

import arrow.core.Either
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Schema
import no.oslokommune.ombruk.shared.error.ValidationError
import no.oslokommune.ombruk.shared.form.IForm
import no.oslokommune.ombruk.shared.utils.validation.isGreaterThanStartDateTime
import no.oslokommune.ombruk.shared.utils.validation.isAtLeastOnePropertySet
import no.oslokommune.ombruk.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThan
import org.valiktor.validate
import java.time.LocalDateTime

@KtorExperimentalLocationsAPI
@Location("/")
data class UttakDeleteForm(
    @get:Parameter(
        `in` = ParameterIn.QUERY,
        name = "id",
        schema = Schema(type = "int32"),
        description = "ID of the specific Uttak to delete",
        required = false
    ) var id: Int? = null,
    @get:Parameter(
        `in` = ParameterIn.QUERY,
        name = "gjentakelsesRegelId",
        schema = Schema(type = "int32"),
        description = "Delete all Uttak with the specified GjentakelsesRegel ID",
        required = false
    ) var gjentakelsesRegelId: Int? = null,
    @get:Parameter(
        `in` = ParameterIn.QUERY,
        name = "startTidspunkt",
        schema = Schema(type = "string", format = "date-time"),
        description = "Delete all Uttak after the specified date. Inclusive",
        required = false
    ) var startTidspunkt: LocalDateTime? = null,
    @get:Parameter(
        `in` = ParameterIn.QUERY,
        name = "sluttTidspunkt",
        schema = Schema(type = "string", format = "date-time"),
        description = "Delete all Uttak before the specified date. Inclusive",
        required = false
    ) var sluttTidspunkt: LocalDateTime? = null,
    @get:Parameter(
        `in` = ParameterIn.QUERY,
        name = "stasjonId",
        schema = Schema(type = "int32"),
        description = "Delete all Uttak for this station",
        required = false
    ) var stasjonId: Int? = null,
    @get:Parameter(
        `in` = ParameterIn.QUERY,
        name = "partnerId",
        schema = Schema(type = "int32"),
        description = "Delete all Uttak for this partner",
        required = false
    ) var partnerId: Int? = null
) : IForm<UttakDeleteForm> {
    override fun validOrError(): Either<ValidationError, UttakDeleteForm> = runCatchingValidation {
        validate(this) {

            val properties = listOf(
                id,
                gjentakelsesRegelId,
                startTidspunkt,
                sluttTidspunkt,
                stasjonId,
                partnerId
            )

            isAtLeastOnePropertySet(properties)

            validate(UttakDeleteForm::id).isGreaterThan(0)
            validate(UttakDeleteForm::gjentakelsesRegelId).isGreaterThan(0)
            validate(UttakDeleteForm::stasjonId).isGreaterThan(0)
            validate(UttakDeleteForm::partnerId).isGreaterThan(0)

            validate(UttakDeleteForm::sluttTidspunkt).isGreaterThanStartDateTime(startTidspunkt)
        }
    }
}
