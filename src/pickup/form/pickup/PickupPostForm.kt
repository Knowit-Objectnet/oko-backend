package ombruk.backend.pickup.form.pickup

import arrow.core.Either
import kotlinx.serialization.Serializable
import ombruk.backend.calendar.database.StationRepository
import ombruk.backend.shared.error.ValidationError
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.model.serializer.LocalDateTimeSerializer
import ombruk.backend.shared.utils.validation.isInRepository
import ombruk.backend.shared.utils.validation.isLessThanEndDateTime
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThan
import org.valiktor.functions.isNotBlank
import org.valiktor.validate
import java.time.LocalDateTime


@Serializable
data class PickupPostForm(
    @Serializable(with = LocalDateTimeSerializer::class) var startDateTime: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class) var endDateTime: LocalDateTime,
    val description: String? = null,
    val stationId: Int
) : IForm<PickupPostForm> {
    override fun validOrError(): Either<ValidationError, PickupPostForm> = runCatchingValidation {
        validate(this) {
            validate(PickupPostForm::stationId).isGreaterThan(0)
            validate(PickupPostForm::stationId).isInRepository(StationRepository)
            validate(PickupPostForm::description).isNotBlank()
            validate(PickupPostForm::startDateTime).isLessThanEndDateTime(endDateTime)
        }
    }
}