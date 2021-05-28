package ombruk.backend.calendar.form.station

import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isNotBlank
import org.valiktor.validate

@KtorExperimentalLocationsAPI
@Location("")
data class StationGetForm(val name: String? = null) : IForm<StationGetForm> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(StationGetForm::name).isNotBlank()
        }
    }
}