package ombruk.backend.calendar.form.event

import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThan
import org.valiktor.validate

@KtorExperimentalLocationsAPI
@Location("/{id}")
data class EventGetByIdForm(val id: Int) : IForm<EventGetByIdForm> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(EventGetByIdForm::id).isGreaterThan(0)
        }
    }
}