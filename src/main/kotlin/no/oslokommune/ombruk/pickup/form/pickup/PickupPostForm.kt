package no.oslokommune.ombruk.pickup.form.pickup

import arrow.core.Either
import kotlinx.serialization.Serializable
import no.oslokommune.ombruk.stasjon.database.StasjonRepository
import no.oslokommune.ombruk.shared.error.ValidationError
import no.oslokommune.ombruk.shared.form.IForm
import no.oslokommune.ombruk.shared.model.serializer.LocalDateTimeSerializer
import no.oslokommune.ombruk.shared.utils.validation.isInRepository
import no.oslokommune.ombruk.shared.utils.validation.isLessThanEndDateTime
import no.oslokommune.ombruk.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThan
import org.valiktor.functions.isNotBlank
import org.valiktor.validate
import java.time.LocalDateTime


@Serializable
data class PickupPostForm(
    @Serializable(with = LocalDateTimeSerializer::class) var startDateTime: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class) var endDateTime: LocalDateTime,
    val description: String? = null,
    val stasjonId: Int
) : IForm<PickupPostForm> {
    override fun validOrError(): Either<ValidationError, PickupPostForm> = runCatchingValidation {
        validate(this) {
            validate(PickupPostForm::stasjonId).isGreaterThan(0)
            validate(PickupPostForm::stasjonId).isInRepository(StasjonRepository)
            validate(PickupPostForm::description).isNotBlank()
            validate(PickupPostForm::startDateTime).isLessThanEndDateTime(endDateTime)
        }
    }
}