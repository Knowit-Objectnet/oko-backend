package ombruk.backend.reporting.form

import io.ktor.locations.Location
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThan
import org.valiktor.validate

@Location("/{id}")
class ReportGetByIdForm(
    var id: Int
) : IForm<ReportGetByIdForm> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(ReportGetByIdForm::id).isGreaterThan(0)

        }
    }
}