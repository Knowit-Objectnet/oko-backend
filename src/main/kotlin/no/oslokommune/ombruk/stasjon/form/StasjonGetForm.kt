package no.oslokommune.ombruk.stasjon.form

import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import no.oslokommune.ombruk.shared.form.IForm
import no.oslokommune.ombruk.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isNotBlank
import org.valiktor.validate

@KtorExperimentalLocationsAPI
@Location("/")
data class StasjonGetForm(val navn: String? = null) : IForm<StasjonGetForm> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(StasjonGetForm::navn).isNotBlank()
        }
    }
}