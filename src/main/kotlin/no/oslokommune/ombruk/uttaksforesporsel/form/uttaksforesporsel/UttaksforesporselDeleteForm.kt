package no.oslokommune.ombruk.uttaksforesporsel.form.uttaksforesporsel

import arrow.core.Either
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import no.oslokommune.ombruk.partner.database.PartnerRepository
import no.oslokommune.ombruk.uttaksforesporsel.database.PickupRepository
import no.oslokommune.ombruk.shared.error.ValidationError
import no.oslokommune.ombruk.shared.form.IForm
import no.oslokommune.ombruk.shared.utils.validation.isInRepository
import no.oslokommune.ombruk.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThan
import org.valiktor.validate

@KtorExperimentalLocationsAPI
@Location("/")
data class UttaksforesporselDeleteForm(
    val pickupId: Int,
    val partnerId: Int
) : IForm<UttaksforesporselDeleteForm> {
    override fun validOrError(): Either<ValidationError, UttaksforesporselDeleteForm> = runCatchingValidation {
        validate(this) {
            validate(UttaksforesporselDeleteForm::pickupId).isGreaterThan(0)
            validate(UttaksforesporselDeleteForm::partnerId).isGreaterThan(0)
            validate(UttaksforesporselDeleteForm::pickupId).isInRepository(PickupRepository)
            validate(UttaksforesporselDeleteForm::partnerId).isInRepository(PartnerRepository)
        }
    }
}