package ombruk.backend.henting.application.api.dto

import arrow.core.Either
import kotlinx.serialization.Serializable
import ombruk.backend.henting.domain.model.HenteplanFrekvens
import ombruk.backend.henting.domain.params.HenteplanUpdateParams
import ombruk.backend.shared.error.ValidationError
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.model.serializer.LocalDateTimeSerializer
import ombruk.backend.shared.utils.validation.isGreaterThanStartDateTime
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isPositive
import org.valiktor.validate
import shared.model.serializer.UUIDSerializer
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.util.*

@Serializable(with = UUIDSerializer::class)
data class HenteplanUpdateDto(
    override val id: UUID,
    override val frekvens: HenteplanFrekvens? = null,
    @Serializable(with = LocalDateTimeSerializer::class) override val startTidspunkt: LocalDateTime? = null,
    @Serializable(with = LocalDateTimeSerializer::class) override val sluttTidspunkt: LocalDateTime? = null,
    override val ukeDag: DayOfWeek? = null,
    override val merknad: String? = null
) : IForm<HenteplanUpdateDto>, HenteplanUpdateParams() {
    override fun validOrError(): Either<ValidationError, HenteplanUpdateDto> = runCatchingValidation {
        validate(this) {
            if (startTidspunkt != null && sluttTidspunkt != null) {
                validate(HenteplanUpdateDto::sluttTidspunkt).isGreaterThanStartDateTime(startTidspunkt)
            }
        }
    }
}