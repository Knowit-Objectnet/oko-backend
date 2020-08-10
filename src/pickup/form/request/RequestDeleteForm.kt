package ombruk.backend.pickup.form.request

import arrow.core.Either
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import ombruk.backend.partner.database.PartnerRepository
import ombruk.backend.pickup.database.PickupRepository
import ombruk.backend.shared.error.ValidationError
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.isInRepository
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThan
import org.valiktor.validate

@KtorExperimentalLocationsAPI
@Location("/")
data class RequestDeleteForm (
    val pickupId: Int,
    val partnerId: Int
) : IForm<RequestDeleteForm> {
    override fun validOrError(): Either<ValidationError, RequestDeleteForm> = runCatchingValidation{
        validate(this) {
            validate(RequestDeleteForm::pickupId).isGreaterThan(0)
            validate(RequestDeleteForm::partnerId).isGreaterThan(0)
            validate(RequestDeleteForm::pickupId).isInRepository(PickupRepository)
            validate(RequestDeleteForm::partnerId).isInRepository(PartnerRepository)
        }
    }
}