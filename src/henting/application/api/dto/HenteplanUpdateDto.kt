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
import java.time.DayOfWeek
import java.time.LocalDateTime

@Serializable
data class HenteplanUpdateDto(
    override val id: Int,
    override val stasjonId: Int?,
    override val frekvens: HenteplanFrekvens?,
    @Serializable(with = LocalDateTimeSerializer::class) override val startTidspunkt: LocalDateTime?,
    @Serializable(with = LocalDateTimeSerializer::class) override val sluttTidspunkt: LocalDateTime?,
    override val ukeDag: DayOfWeek?,
    override val merknad: String?
) : IForm<HenteplanUpdateDto>, HenteplanUpdateParams() {
    override fun validOrError(): Either<ValidationError, HenteplanUpdateDto> = runCatchingValidation{
        validate(this) {
            validate(HenteplanUpdateDto::id).isPositive()
            stasjonId?.let { validate(HenteplanUpdateDto::stasjonId).isPositive() }
            if(startTidspunkt != null && sluttTidspunkt != null ) {
                validate(HenteplanUpdateDto::sluttTidspunkt).isGreaterThanStartDateTime(startTidspunkt)
            }
        }
    }
}