package no.oslokommune.ombruk.partner.form

import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import no.oslokommune.ombruk.shared.form.IForm
import no.oslokommune.ombruk.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isNotBlank
import org.valiktor.validate

@KtorExperimentalLocationsAPI
@Location("/")
data class PartnerGetForm(
    var name: String? = null
) : IForm<PartnerGetForm> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(PartnerGetForm::name).isNotBlank()
        }
    }
}