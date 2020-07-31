package ombruk.backend.partner.form

import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThan
import org.valiktor.validate

@KtorExperimentalLocationsAPI
@Location("/{id}")
data class PartnerDeleteForm(val id: Int) : IForm<PartnerDeleteForm> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(PartnerDeleteForm::id).isGreaterThan(0)
        }
    }

}