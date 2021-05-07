package henting.application.api.dto

import arrow.core.Either
import kotlinx.serialization.Serializable
import ombruk.backend.henting.domain.model.HenteplanFrekvens
import ombruk.backend.henting.domain.params.HenteplanCreateParams
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
data class HenteplanPostDto(
    override val avtaleId: UUID,
    override val stasjonId: UUID,
    override val frekvens: HenteplanFrekvens,
    @Serializable(with = LocalDateTimeSerializer::class) override val startTidspunkt: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class) override val sluttTidspunkt: LocalDateTime,
    override val ukedag: DayOfWeek,
    override val merknad: String?
) : IForm<HenteplanPostDto>, HenteplanCreateParams() {
    override fun validOrError(): Either<ValidationError, HenteplanPostDto> = runCatchingValidation {
        validate(this) {
            validate(HenteplanPostDto::sluttTidspunkt).isGreaterThanStartDateTime(startTidspunkt)
        }
    }
}