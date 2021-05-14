package ombruk.backend.henting.application.api.dto

import arrow.core.Either
import kotlinx.serialization.Serializable
import ombruk.backend.henting.domain.params.PlanlagtHentingUpdateParams
import ombruk.backend.shared.error.ValidationError
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.model.serializer.LocalDateTimeSerializer
import ombruk.backend.shared.utils.validation.isGreaterThanStartDateTime
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.validate
import shared.model.serializer.UUIDSerializer
import java.time.LocalDateTime
import java.util.*

@Serializable(with = UUIDSerializer::class)
data class PlanlagtHentingUpdateDto(
    override val id: UUID,
    @Serializable(with = LocalDateTimeSerializer::class) override val startTidspunkt: LocalDateTime? = null,
    @Serializable(with = LocalDateTimeSerializer::class) override val sluttTidspunkt: LocalDateTime? = null,
    override val merknad: String? = null,
    @Serializable(with = LocalDateTimeSerializer::class) override val avlyst: LocalDateTime? = null
) : IForm<PlanlagtHentingUpdateDto>, PlanlagtHentingUpdateParams() {
    override fun validOrError(): Either<ValidationError, PlanlagtHentingUpdateDto> = runCatchingValidation {
        validate(this) {
            if (startTidspunkt != null && sluttTidspunkt != null) {
                validate(PlanlagtHentingUpdateDto::sluttTidspunkt).isGreaterThanStartDateTime(startTidspunkt)
            }
        }
    }
}