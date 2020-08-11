package ombruk.backend.partner.form

import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.runCatchingValidation
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