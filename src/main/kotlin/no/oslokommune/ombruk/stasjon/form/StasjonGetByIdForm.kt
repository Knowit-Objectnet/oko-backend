package no.oslokommune.ombruk.stasjon.form

import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import no.oslokommune.ombruk.shared.form.IForm
import no.oslokommune.ombruk.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThan
import org.valiktor.validate

@KtorExperimentalLocationsAPI
@Location("/{id}")
data class StasjonGetByIdForm(val id: Int) : IForm<StasjonGetByIdForm> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(StasjonGetByIdForm::id).isGreaterThan(0)
        }
    }
}