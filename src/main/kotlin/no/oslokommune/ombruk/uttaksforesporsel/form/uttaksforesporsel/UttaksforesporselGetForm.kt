package no.oslokommune.ombruk.uttaksforesporsel.form.uttaksforesporsel

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
data class UttaksforesporselGetForm(
    // if none are set, get all requests.
    val pickupId: Int? = null,  // if set, get all requests for a no.oslokommune.ombruk.pickup.
    val partnerId: Int? = null  // if set, get all requests for a partner
) : IForm<UttaksforesporselGetForm> {
    override fun validOrError(): Either<ValidationError, UttaksforesporselGetForm> = runCatchingValidation {
        validate(this) {
            validate(UttaksforesporselGetForm::pickupId).isGreaterThan(0)
            validate(UttaksforesporselGetForm::partnerId).isGreaterThan(0)
        }
    }
}