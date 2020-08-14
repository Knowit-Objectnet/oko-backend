package ombruk.backend.calendar.form.station

import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThan
import org.valiktor.validate

@KtorExperimentalLocationsAPI
@Location("/{id}")
data class StationGetByIdForm(val id: Int) : IForm<StationGetByIdForm> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(StationGetByIdForm::id).isGreaterThan(0)
        }
    }
}