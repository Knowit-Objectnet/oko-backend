package ombruk.backend.pickup.form.pickup

import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import ombruk.backend.partner.database.PartnerRepository
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.isGreaterThanStartDateTime
import ombruk.backend.shared.utils.validation.isInRepository
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThan
import org.valiktor.validate
import java.time.LocalDateTime

@KtorExperimentalLocationsAPI
@Location("/")
data class PickupGetForm (
    var startDateTime: LocalDateTime? = null,
    var endDateTime: LocalDateTime? = null,
    val stationId: Int? = null,
    val partnerId: Int? = null
) : IForm<PickupGetForm> {
    override fun validOrError() = runCatchingValidation {
        validate(this){
            if(stationId != null) {
                validate(PickupGetForm::stationId).isGreaterThan(0)
            }
            if(startDateTime != null && endDateTime != null) {
                validate(PickupGetForm::endDateTime).isGreaterThanStartDateTime(startDateTime)
            }

            if(partnerId != null){
                validate(PickupGetForm::partnerId).isGreaterThan(0)
                validate(PickupGetForm::partnerId).isInRepository(PartnerRepository)
            }
        }
    }
}