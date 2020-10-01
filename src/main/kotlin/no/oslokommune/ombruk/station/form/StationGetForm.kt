package no.oslokommune.ombruk.station.form

import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import no.oslokommune.ombruk.shared.form.IForm
import no.oslokommune.ombruk.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isNotBlank
import org.valiktor.validate

@KtorExperimentalLocationsAPI
@Location("/")
data class StationGetForm(val name: String? = null) : IForm<StationGetForm> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(StationGetForm::name).isNotBlank()
        }
    }
}