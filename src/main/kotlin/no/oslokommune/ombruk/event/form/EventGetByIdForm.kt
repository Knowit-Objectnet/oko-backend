package no.oslokommune.ombruk.event.form

import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import no.oslokommune.ombruk.shared.form.IForm
import no.oslokommune.ombruk.shared.utils.validation.runCatchingValidation
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