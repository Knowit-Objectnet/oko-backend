package no.oslokommune.ombruk.uttaksforesporsel.form.uttaksforesporsel

import arrow.core.Either
import kotlinx.serialization.Serializable
import no.oslokommune.ombruk.partner.database.PartnerRepository
import no.oslokommune.ombruk.uttaksforesporsel.database.PickupRepository
import no.oslokommune.ombruk.shared.error.ValidationError
import no.oslokommune.ombruk.shared.form.IForm
import no.oslokommune.ombruk.shared.utils.validation.isInRepository
import no.oslokommune.ombruk.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThan
import org.valiktor.validate

@Serializable
data class UttaksforesporselPostForm(
    val pickupId: Int,
    val partnerId: Int
) : IForm<UttaksforesporselPostForm> {
    override fun validOrError(): Either<ValidationError, UttaksforesporselPostForm> = runCatchingValidation {
        validate(this) {
            validate(UttaksforesporselPostForm::pickupId).isGreaterThan(0)
            validate(UttaksforesporselPostForm::partnerId).isGreaterThan(0)
            validate(UttaksforesporselPostForm::pickupId).isInRepository(PickupRepository)
            validate(UttaksforesporselPostForm::partnerId).isInRepository(PartnerRepository)
        }
    }
}