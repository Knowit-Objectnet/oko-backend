package no.oslokommune.ombruk.pickup.form.pickup

import arrow.core.Either
import kotlinx.serialization.Serializable
import no.oslokommune.ombruk.partner.database.PartnerRepository
import no.oslokommune.ombruk.pickup.database.PickupRepository
import no.oslokommune.ombruk.shared.error.ValidationError
import no.oslokommune.ombruk.shared.form.IForm
import no.oslokommune.ombruk.shared.model.serializer.LocalDateTimeSerializer
import no.oslokommune.ombruk.shared.utils.validation.isGreaterThanStartDateTime
import no.oslokommune.ombruk.shared.utils.validation.isInRepository
import no.oslokommune.ombruk.shared.utils.validation.isLessThanEndDateTime
import no.oslokommune.ombruk.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThan
import org.valiktor.functions.isNotBlank
import org.valiktor.validate
import java.time.LocalDateTime


@Serializable
data class PickupUpdateForm(
    val id: Int,
    @Serializable(with = LocalDateTimeSerializer::class) var startDateTime: LocalDateTime? = null,
    @Serializable(with = LocalDateTimeSerializer::class) var endDateTime: LocalDateTime? = null,
    val description: String? = null,
    val chosenPartnerId: Int? = null
) : IForm<PickupUpdateForm> {
    override fun validOrError(): Either<ValidationError, PickupUpdateForm> = runCatchingValidation {
        validate(this) {
            validate(PickupUpdateForm::id).isGreaterThan(0)
            validate(PickupUpdateForm::description).isNotBlank()
            validate(PickupUpdateForm::chosenPartnerId).isGreaterThan(0)
            validate(PickupUpdateForm::chosenPartnerId).isInRepository(PartnerRepository)

            if (startDateTime != null || endDateTime != null) {      // Only do this db call if there's a chance of updating.
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