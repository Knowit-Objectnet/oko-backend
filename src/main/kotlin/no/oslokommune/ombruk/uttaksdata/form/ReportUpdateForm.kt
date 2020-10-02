package no.oslokommune.ombruk.uttaksdata.form

import kotlinx.serialization.Serializable
import no.oslokommune.ombruk.shared.form.IForm
import no.oslokommune.ombruk.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThan
import org.valiktor.validate

@Serializable
data class ReportUpdateForm(val id: Int, val weight: Int) : IForm<ReportUpdateForm> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(ReportUpdateForm::id).isGreaterThan(0)
            validate(ReportUpdateForm::weight).isGreaterThan(0)
        }
    }

}