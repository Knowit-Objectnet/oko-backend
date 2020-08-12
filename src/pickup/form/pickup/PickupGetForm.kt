package ombruk.backend.pickup.form.pickup

import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.isGreaterThanStartDateTime
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThan
import org.valiktor.validate
import java.time.LocalDateTime

@KtorExperimentalLocationsAPI
@Location("/")
data class PickupGetForm(
    var startDateTime: LocalDateTime? = null,   // get from this date.
    var endDateTime: LocalDateTime? = null,     // get until this date. Optionally, specify start and end for query in range.
    val stationId: Int? = null,
    val partnerId: Int? = null
) : IForm<PickupGetForm> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(PickupGetForm::stationId).isGreaterThan(0)
            validate(PickupGetForm::partnerId).isGreaterThan(0)
            validate(PickupGetForm::endDateTime).isGreaterThanStartDateTime(startDateTime)
        }
    }
}