package no.oslokommune.ombruk.reporting.form

import io.ktor.locations.Location
import no.oslokommune.ombruk.shared.form.IForm
import no.oslokommune.ombruk.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThan
import org.valiktor.validate

@Location("/{id}")
data class ReportGetByIdForm(
    var id: Int
) : IForm<ReportGetByIdForm> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(ReportGetByIdForm::id).isGreaterThan(0)

        }
    }
}