package no.oslokommune.ombruk.pickup.form.request

import arrow.core.Either
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import no.oslokommune.ombruk.shared.error.ValidationError
import no.oslokommune.ombruk.shared.form.IForm
import no.oslokommune.ombruk.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThan
import org.valiktor.validate

@KtorExperimentalLocationsAPI
@Location("/")
data class RequestGetForm(
    // if none are set, get all requests.
    val pickupId: Int? = null,  // if set, get all requests for a no.oslokommune.ombruk.pickup.
    val partnerId: Int? = null  // if set, get all requests for a partner
) : IForm<RequestGetForm> {
    override fun validOrError(): Either<ValidationError, RequestGetForm> = runCatchingValidation {
        validate(this) {
            validate(RequestGetForm::pickupId).isGreaterThan(0)
            validate(RequestGetForm::partnerId).isGreaterThan(0)
        }
    }
}