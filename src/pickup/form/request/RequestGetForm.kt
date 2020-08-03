package ombruk.backend.pickup.form.request

import arrow.core.Either
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import ombruk.backend.shared.error.ValidationError
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThan
import org.valiktor.validate

@KtorExperimentalLocationsAPI
@Location("/")
data class RequestGetForm (
    val pickupId: Int? = null,
    val partnerId: Int? = null
) : IForm<RequestGetForm> {
    override fun validOrError(): Either<ValidationError, RequestGetForm> = runCatchingValidation{
        validate(this) {
            validate(RequestGetForm::pickupId).isGreaterThan(0)
            validate(RequestGetForm::partnerId).isGreaterThan(0)
        }
    }
}