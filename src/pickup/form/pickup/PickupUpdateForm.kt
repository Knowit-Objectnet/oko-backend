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
import org.valiktor.functions.isNotNull
import org.valiktor.validate
import java.time.LocalDateTime


@Serializable
data class PickupUpdateForm (
    val id: Int,
    @Serializable(with = LocalDateTimeSerializer::class) var startDateTime: LocalDateTime? = null,
    @Serializable(with = LocalDateTimeSerializer::class) var endDateTime: LocalDateTime? = null,
    val description: String? = null,
    val chosenPartnerID: Int? = null
) : IForm<PickupUpdateForm> {
    override fun validOrError(): Either<ValidationError, PickupUpdateForm> = runCatchingValidation {
        validate(this) {
            validate(PickupUpdateForm::id).isGreaterThan(0)

            chosenPartnerID?.let {
                validate(PickupUpdateForm::chosenPartnerID).isGreaterThan(0)
                validate(PickupUpdateForm::chosenPartnerID).isInRepository(PartnerRepository)
            }

            PickupRepository.getPickupById(id).map { pickup ->
                val newStartDateTime = startDateTime ?: pickup.startDateTime
                val newEndDateTime = endDateTime ?: pickup.endDateTime
                validate(PickupUpdateForm::startDateTime).isLessThanEndDateTime(newStartDateTime)
                validate(PickupUpdateForm::endDateTime).isGreaterThanStartDateTime(newEndDateTime)
            }
        }
    }
}