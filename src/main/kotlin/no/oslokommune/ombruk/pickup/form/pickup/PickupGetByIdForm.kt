package no.oslokommune.ombruk.pickup.form.pickup

import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import no.oslokommune.ombruk.shared.form.IForm
import no.oslokommune.ombruk.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThan
import org.valiktor.validate

@KtorExperimentalLocationsAPI
@Location("/{id}")
data class PickupGetByIdForm(val id: Int) : IForm<PickupGetByIdForm> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(PickupGetByIdForm::id).isGreaterThan(0)
        }
    }
}