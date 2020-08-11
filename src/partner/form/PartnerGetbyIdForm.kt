package ombruk.backend.partner.form

import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThan
import org.valiktor.validate

@KtorExperimentalLocationsAPI
@Location("/{id}")
data class PartnerGetByIdForm(val id: Int) : IForm<PartnerGetByIdForm> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(PartnerGetByIdForm::id).isGreaterThan(0)
        }
    }
}