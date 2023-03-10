package ombruk.backend.henting.application.api.dto

import arrow.core.Either
import kotlinx.serialization.Serializable
import ombruk.backend.henting.domain.params.PlanlagtHentingCreateParams
import ombruk.backend.shared.error.ValidationError
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.model.serializer.LocalDateTimeSerializer
import ombruk.backend.shared.utils.validation.isGreaterThanStartDateTime
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.validate
import shared.model.serializer.UUIDSerializer
import java.time.LocalDateTime
import java.util.*

@Serializable
data class PlanlagtHentingSaveDto(
    @Serializable(with = LocalDateTimeSerializer::class) override val startTidspunkt: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class) override val sluttTidspunkt: LocalDateTime,
    @Serializable(with = UUIDSerializer::class) override val henteplanId: UUID
) : IForm<PlanlagtHentingSaveDto>, PlanlagtHentingCreateParams() {
    override fun validOrError(): Either<ValidationError, PlanlagtHentingSaveDto> = runCatchingValidation {
        validate(this) {
            validate(PlanlagtHentingSaveDto::sluttTidspunkt).isGreaterThanStartDateTime(startTidspunkt)
        }
    }
}