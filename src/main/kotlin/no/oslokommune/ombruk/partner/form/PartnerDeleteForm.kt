package no.oslokommune.ombruk.partner.form

import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import no.oslokommune.ombruk.shared.form.IForm
import no.oslokommune.ombruk.shared.utils.validation.runCatchingValidation
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