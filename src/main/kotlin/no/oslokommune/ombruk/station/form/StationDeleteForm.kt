package no.oslokommune.ombruk.station.form

import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import no.oslokommune.ombruk.shared.form.IForm
import no.oslokommune.ombruk.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThan
import org.valiktor.validate

@KtorExperimentalLocationsAPI
@Location("/{id}")
data class StationDeleteForm(val id: Int) : IForm<StationDeleteForm> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(StationDeleteForm::id).isGreaterThan(0)
        }
    }

}