package no.oslokommune.ombruk.pickup.form.pickup

import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import no.oslokommune.ombruk.shared.form.IForm
import no.oslokommune.ombruk.shared.utils.validation.isGreaterThanStartDateTime
import no.oslokommune.ombruk.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThan
import org.valiktor.validate
import java.time.LocalDateTime

@KtorExperimentalLocationsAPI
@Location("/")
data class PickupGetForm(
    var startDateTime: LocalDateTime? = null,   // get from this date.
    var endDateTime: LocalDateTime? = null,     // get until this date. Optionally, specify start and end for query in range.
    val stasjonId: Int? = null,
    val partnerId: Int? = null
) : IForm<PickupGetForm> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(PickupGetForm::stasjonId).isGreaterThan(0)
            validate(PickupGetForm::partnerId).isGreaterThan(0)
            validate(PickupGetForm::endDateTime).isGreaterThanStartDateTime(startDateTime)
        }
    }
}