package no.oslokommune.ombruk.pickup.form.request

import arrow.core.Either
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import no.oslokommune.ombruk.partner.database.PartnerRepository
import no.oslokommune.ombruk.pickup.database.PickupRepository
import no.oslokommune.ombruk.shared.error.ValidationError
import no.oslokommune.ombruk.shared.form.IForm
import no.oslokommune.ombruk.shared.utils.validation.isInRepository
import no.oslokommune.ombruk.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThan
import org.valiktor.validate

@KtorExperimentalLocationsAPI
@Location("/")
data class RequestDeleteForm(
    val pickupId: Int,
    val partnerId: Int
) : IForm<RequestDeleteForm> {
    override fun validOrError(): Either<ValidationError, RequestDeleteForm> = runCatchingValidation {
        validate(this) {
            validate(RequestDeleteForm::pickupId).isGreaterThan(0)
            validate(RequestDeleteForm::partnerId).isGreaterThan(0)
            validate(RequestDeleteForm::pickupId).isInRepository(PickupRepository)
            validate(RequestDeleteForm::partnerId).isInRepository(PartnerRepository)
        }
    }
}