package no.oslokommune.ombruk.stasjon.form

import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import no.oslokommune.ombruk.shared.form.IForm
import no.oslokommune.ombruk.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThan
import org.valiktor.validate

@KtorExperimentalLocationsAPI
@Location("/{id}")
data class StasjonDeleteForm(val id: Int) : IForm<StasjonDeleteForm> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(StasjonDeleteForm::id).isGreaterThan(0)
        }
    }

}