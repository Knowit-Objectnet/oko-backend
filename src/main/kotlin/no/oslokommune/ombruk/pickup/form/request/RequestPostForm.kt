package no.oslokommune.ombruk.pickup.form.request

import arrow.core.Either
import kotlinx.serialization.Serializable
import no.oslokommune.ombruk.partner.database.PartnerRepository
import no.oslokommune.ombruk.pickup.database.PickupRepository
import no.oslokommune.ombruk.shared.error.ValidationError
import no.oslokommune.ombruk.shared.form.IForm
import no.oslokommune.ombruk.shared.utils.validation.isInRepository
import no.oslokommune.ombruk.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThan
import org.valiktor.validate

@Serializable
data class RequestPostForm(
    val pickupId: Int,
    val partnerId: Int
) : IForm<RequestPostForm> {
    override fun validOrError(): Either<ValidationError, RequestPostForm> = runCatchingValidation {
        validate(this) {
            validate(RequestPostForm::pickupId).isGreaterThan(0)
            validate(RequestPostForm::partnerId).isGreaterThan(0)
            validate(RequestPostForm::pickupId).isInRepository(PickupRepository)
            validate(RequestPostForm::partnerId).isInRepository(PartnerRepository)
        }
    }
}