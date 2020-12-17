package no.oslokommune.ombruk.uttaksforesporsel.form.uttaksforesporsel

import arrow.core.Either
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Schema
import no.oslokommune.ombruk.partner.database.PartnerRepository
import no.oslokommune.ombruk.shared.error.ValidationError
import no.oslokommune.ombruk.shared.form.IForm
import no.oslokommune.ombruk.shared.utils.validation.isInRepository
import no.oslokommune.ombruk.shared.utils.validation.runCatchingValidation
import no.oslokommune.ombruk.uttak.database.UttakRepository
import org.valiktor.functions.isGreaterThan
import org.valiktor.validate

@KtorExperimentalLocationsAPI
@Location("/")
data class UttaksforesporselDeleteForm(
    @get:Parameter(
        `in` = ParameterIn.QUERY,
        name = "uttaksId",
        description = "The Uttak ID of the Uttaksforesporsel to delete",
        required = true,
        schema = Schema(type = "int32")
    ) val uttaksId: Int,
    @get:Parameter(
        `in` = ParameterIn.QUERY,
        name = "partnerId",
        description = "The Partner ID of the Uttaksforesporsel to delete",
        required = true,
        schema = Schema(type = "int32")
    ) val partnerId: Int
) : IForm<UttaksforesporselDeleteForm> {
    override fun validOrError(): Either<ValidationError, UttaksforesporselDeleteForm> = runCatchingValidation {
        validate(this) {
            validate(UttaksforesporselDeleteForm::uttaksId).isGreaterThan(0)
            validate(UttaksforesporselDeleteForm::partnerId).isGreaterThan(0)
            validate(UttaksforesporselDeleteForm::uttaksId).isInRepository(UttakRepository)
            validate(UttaksforesporselDeleteForm::partnerId).isInRepository(PartnerRepository)
        }
    }
}