package ombruk.backend.henting.application.api.dto

import arrow.core.Either
import kotlinx.serialization.Serializable
import ombruk.backend.henting.domain.params.EkstraHentingCreateParams
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
data class EkstraHentingSaveDto(
    @Serializable(with = LocalDateTimeSerializer::class) override val startTidspunkt: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class) override val sluttTidspunkt: LocalDateTime,
    override val merknad: String?,
    @Serializable(with = UUIDSerializer::class) override val stasjonId: UUID
) : IForm<EkstraHentingSaveDto>, EkstraHentingCreateParams() {
    override fun validOrError(): Either<ValidationError, EkstraHentingSaveDto> = runCatchingValidation {
        validate(this) {
            validate(EkstraHentingSaveDto::sluttTidspunkt).isGreaterThanStartDateTime(startTidspunkt)
        }
    }
}