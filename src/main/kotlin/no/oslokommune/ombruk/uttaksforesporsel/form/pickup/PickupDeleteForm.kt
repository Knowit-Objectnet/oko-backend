package no.oslokommune.ombruk.uttaksforesporsel.form.pickup

import arrow.core.Either
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import no.oslokommune.ombruk.shared.error.ValidationError
import no.oslokommune.ombruk.shared.form.IForm
import no.oslokommune.ombruk.shared.utils.validation.runCatchingValidation
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