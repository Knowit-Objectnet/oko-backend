package ombruk.backend.reporting.form

import io.ktor.locations.Location
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.isLessThanEndDateTime
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThan
import org.valiktor.functions.isNull
import org.valiktor.validate
import java.time.LocalDateTime

@Location("/")
class ReportGetForm(
    var eventID: Int? = null,
    var stationID: Int? = null,
    var partnerID: Int? = null,
    var fromDate: LocalDateTime? = null,
    var toDate: LocalDateTime? = null
) : IForm<ReportGetForm> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(ReportGetForm::eventID).isGreaterThan(0)
            validate(ReportGetForm::stationID).isGreaterThan(0)
            validate(ReportGetForm::partnerID).isGreaterThan(0)

            validate(ReportGetForm::fromDate).isLessThanEndDateTime(toDate)

            if (eventID != null) {
                validate(ReportGetForm::stationID).isNull()
                validate(ReportGetForm::partnerID).isNull()
                validate(ReportGetForm::fromDate).isNull()
                validate(ReportGetForm::toDate).isNull()
            }
        }
    }
}