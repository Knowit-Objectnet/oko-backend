package ombruk.backend.henting.application.api.dto

import arrow.core.Either
import io.ktor.locations.*
import kotlinx.serialization.Serializable
import ombruk.backend.henting.domain.model.HenteplanFrekvens
import ombruk.backend.henting.domain.params.HenteplanFindParams
import ombruk.backend.shared.error.ValidationError
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.model.serializer.LocalDateTimeSerializer
import ombruk.backend.shared.utils.validation.isGreaterThanStartDateTime
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThanOrEqualTo
import org.valiktor.functions.isPositive
import org.valiktor.validate
import shared.model.serializer.UUIDSerializer
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.util.*

@KtorExperimentalLocationsAPI
@Serializable
@Location("/")
data class HenteplanFindDto(
    @Serializable(with = UUIDSerializer::class) override val avtaleId: UUID? = null,
    @Serializable(with = UUIDSerializer::class) override val stasjonId: UUID? = null,
    override val frekvens: HenteplanFrekvens? = null,
    @Serializable(with = LocalDateTimeSerializer::class) override val before: LocalDateTime? = null,
    @Serializable(with = LocalDateTimeSerializer::class) override val after: LocalDateTime? = null,
    override val ukedag: DayOfWeek? = null,
    @Serializable(with = UUIDSerializer::class) override val id: UUID? = null,
) : IForm<HenteplanFindDto>, HenteplanFindParams() {
    override fun validOrError(): Either<ValidationError, HenteplanFindDto> = runCatchingValidation {
        validate(this) {
            if(before != null && after != null) {
                validate(HenteplanFindDto::before).isGreaterThanOrEqualTo(after)
            }
        }
    }
}