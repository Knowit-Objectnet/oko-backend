package ombruk.backend.reporting.form

import kotlinx.serialization.Serializable
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.runCatchingValidation
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