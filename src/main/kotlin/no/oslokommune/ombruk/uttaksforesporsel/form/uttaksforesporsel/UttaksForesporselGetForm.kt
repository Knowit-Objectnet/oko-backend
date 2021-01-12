package no.oslokommune.ombruk.uttaksforesporsel.form.uttaksforesporsel

import arrow.core.Either
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Schema
import no.oslokommune.ombruk.shared.error.ValidationError
import no.oslokommune.ombruk.shared.form.IForm
import no.oslokommune.ombruk.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThan
import org.valiktor.validate
@KtorExperimentalLocationsAPI
@Location("/")
data class UttaksForesporselGetForm(
    // if none are set, get all Foresporsler.
    @get:Parameter(
        `in` = ParameterIn.QUERY,
        name = "uttakId",
        description = "Get all UttaksForesporsel that are associated with this UttaksForesporsel ID",
        schema = Schema(type = "int32")
    ) val uttakId: Int? = null,  // if set, get all foresporsler for an [Uttak].
    @get:Parameter(
        `in` = ParameterIn.QUERY,
        name = "partnerId",
        description = "Get all UttaksForesporsel that are associated with this Partner Id",
        schema = Schema(type = "int32")
    ) val partnerId: Int? = null  // if set, get all foresporsler for a partner
) : IForm<UttaksForesporselGetForm> {
    override fun validOrError(): Either<ValidationError, UttaksForesporselGetForm> = runCatchingValidation {
        validate(this) {
            validate(UttaksForesporselGetForm::uttakId).isGreaterThan(0)
            validate(UttaksForesporselGetForm::partnerId).isGreaterThan(0)
        }
    }
}