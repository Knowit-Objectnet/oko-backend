package ombruk.backend.pickup.form.pickup

import arrow.core.Either
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import ombruk.backend.shared.error.ValidationError
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThan
import org.valiktor.validate

@KtorExperimentalLocationsAPI
@Location("/{id}")
data class PickupDeleteForm(
    val id: Int
) : IForm<PickupDeleteForm> {
    override fun validOrError(): Either<ValidationError, PickupDeleteForm> = runCatchingValidation {
        validate(this) {
            validate(PickupDeleteForm::id).isGreaterThan(0)
        }
    }
}