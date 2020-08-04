package ombruk.backend.pickup.form.request

import arrow.core.Either
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import kotlinx.serialization.Serializable
import ombruk.backend.partner.database.PartnerRepository
import ombruk.backend.pickup.database.PickupRepository
import ombruk.backend.shared.error.ValidationError
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.isInRepository
import ombruk.backend.shared.utils.validation.runCatchingValidation
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