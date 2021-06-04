package ombruk.backend.utlysning.application.api.dto

import arrow.core.Either
import kotlinx.serialization.Serializable
import ombruk.backend.shared.error.ValidationError
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.model.serializer.LocalDateTimeSerializer
import ombruk.backend.shared.utils.validation.runCatchingValidation
import ombruk.backend.utlysning.domain.params.UtlysningCreateParams
import org.valiktor.validate
import shared.model.serializer.UUIDSerializer
import java.time.LocalDateTime
import java.util.*

@Serializable
data class UtlysningSaveDto(
    @Serializable(with = UUIDSerializer::class) override val partnerId: UUID,
    @Serializable(with = UUIDSerializer::class) override val hentingId: UUID,
    @Serializable(with = LocalDateTimeSerializer::class) override val partnerPameldt: LocalDateTime?,
    @Serializable(with = LocalDateTimeSerializer::class) override val stasjonGodkjent: LocalDateTime?,
    override val partnerSkjult: Boolean,
    override val partnerVist: Boolean,
) : IForm<UtlysningSaveDto>, UtlysningCreateParams() {
    override fun validOrError(): Either<ValidationError, UtlysningSaveDto> = runCatchingValidation {
        validate(this) {}
    }
}