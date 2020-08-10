package ombruk.backend.pickup.form.pickup

import arrow.core.Either
import kotlinx.serialization.Serializable
import ombruk.backend.partner.database.PartnerRepository
import ombruk.backend.pickup.database.PickupRepository
import ombruk.backend.shared.error.ValidationError
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.model.serializer.LocalDateTimeSerializer
import ombruk.backend.shared.utils.validation.isGreaterThanStartDateTime
import ombruk.backend.shared.utils.validation.isInRepository
import ombruk.backend.shared.utils.validation.isLessThanEndDateTime
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThan
import org.valiktor.validate
import java.time.LocalDateTime


@Serializable
data class PickupUpdateForm (
    val id: Int,
    @Serializable(with = LocalDateTimeSerializer::class) var startDateTime: LocalDateTime? = null,
    @Serializable(with = LocalDateTimeSerializer::class) var endDateTime: LocalDateTime? = null,
    val description: String? = null,
    val chosenPartnerId: Int? = null
) : IForm<PickupUpdateForm> {
    override fun validOrError(): Either<ValidationError, PickupUpdateForm> = runCatchingValidation {
        validate(this) {
            validate(PickupUpdateForm::id).isGreaterThan(0)

            chosenPartnerId?.let {
                validate(PickupUpdateForm::chosenPartnerId).isGreaterThan(0)
                validate(PickupUpdateForm::chosenPartnerId).isInRepository(PartnerRepository)
            }
            if(startDateTime != null || endDateTime != null) {      // Only do this db call if there's a chance of updating.
                // This essentially ensures that the db won't throw an exception due to broken date constraints (start > end etc).
                PickupRepository.getPickupById(id).map { pickup ->
                    val newStartDateTime = startDateTime ?: pickup.startDateTime
                    val newEndDateTime = endDateTime ?: pickup.endDateTime
                    validate(PickupUpdateForm::startDateTime).isLessThanEndDateTime(newEndDateTime)
                    validate(PickupUpdateForm::endDateTime).isGreaterThanStartDateTime(newStartDateTime)
                }
            }
        }
    }
}