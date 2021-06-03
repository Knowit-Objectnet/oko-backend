package ombruk.backend.utlysning.application.api.dto

import arrow.core.Either
import kotlinx.serialization.Serializable
import ombruk.backend.henting.domain.params.PlanlagtHentingUpdateParams
import ombruk.backend.shared.error.ValidationError
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.model.serializer.LocalDateTimeSerializer
import ombruk.backend.shared.utils.validation.isGreaterThanStartDateTime
import ombruk.backend.shared.utils.validation.runCatchingValidation
import ombruk.backend.utlysning.domain.entity.Utlysning
import ombruk.backend.utlysning.domain.params.UtlysningUpdateParams
import org.valiktor.validate
import shared.model.serializer.UUIDSerializer
import java.time.LocalDateTime
import java.util.*

@Serializable
data class UtlysningUpdateDto(
    @Serializable(with = UUIDSerializer::class) override val id: UUID,
    @Serializable(with = LocalDateTimeSerializer::class) override val partnerPameldt: LocalDateTime? = null,
    @Serializable(with = LocalDateTimeSerializer::class) override val stasjonGodkjent: LocalDateTime? = null,
    override val partnerSkjult: Boolean? = null,
    override val partnerVist: Boolean? = null,
) : IForm<UtlysningUpdateDto>, UtlysningUpdateParams() {
    override fun validOrError(): Either<ValidationError, UtlysningUpdateDto> = runCatchingValidation {
        validate(this) {
        }
    }
}