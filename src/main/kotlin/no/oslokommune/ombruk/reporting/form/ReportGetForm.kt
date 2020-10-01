package no.oslokommune.ombruk.reporting.form

import io.ktor.locations.Location
import no.oslokommune.ombruk.shared.form.IForm
import no.oslokommune.ombruk.shared.utils.validation.isLessThanEndDateTime
import no.oslokommune.ombruk.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThan
import org.valiktor.functions.isNull
import org.valiktor.validate
import java.time.LocalDateTime

@Location("/")
data class ReportGetForm(
    var eventId: Int? = null,
    var stationId: Int? = null,
    var partnerId: Int? = null,
    var fromDate: LocalDateTime? = null,
    var toDate: LocalDateTime? = null
) : IForm<ReportGetForm> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(ReportGetForm::eventId).isGreaterThan(0)
            validate(ReportGetForm::stationId).isGreaterThan(0)
            validate(ReportGetForm::partnerId).isGreaterThan(0)

            validate(ReportGetForm::fromDate).isLessThanEndDateTime(toDate)

            if (eventId != null) {
                validate(ReportGetForm::stationId).isNull()
                validate(ReportGetForm::partnerId).isNull()
                validate(ReportGetForm::fromDate).isNull()
                validate(ReportGetForm::toDate).isNull()
            }
        }
    }
}