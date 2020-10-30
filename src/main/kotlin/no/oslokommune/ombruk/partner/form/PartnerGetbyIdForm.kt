package no.oslokommune.ombruk.partner.form

import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import no.oslokommune.ombruk.shared.form.IForm
import no.oslokommune.ombruk.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThan
import org.valiktor.validate

@KtorExperimentalLocationsAPI
@Location("/{id}") // TODO: remove?
data class PartnerGetByIdForm(val id: Int) : IForm<PartnerGetByIdForm> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(PartnerGetByIdForm::id).isGreaterThan(0)
        }
    }
}