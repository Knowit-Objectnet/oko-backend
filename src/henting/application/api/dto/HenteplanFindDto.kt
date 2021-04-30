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
import org.valiktor.functions.isPositive
import org.valiktor.validate
import java.time.DayOfWeek
import java.time.LocalDateTime

@KtorExperimentalLocationsAPI
@Serializable
@Location("/")
data class HenteplanFindDto(
    override val avtaleId: Int? = null,
    override val stasjonId: Int? = null,
    override val frekvens: HenteplanFrekvens? = null,
    @Serializable(with = LocalDateTimeSerializer::class) override val before: LocalDateTime? = null,
    @Serializable(with = LocalDateTimeSerializer::class) override val after: LocalDateTime? = null,
    override val ukedag: DayOfWeek? = null,
    override val id: Int? = null,
) : IForm<HenteplanFindDto>, HenteplanFindParams() {
    override fun validOrError(): Either<ValidationError, HenteplanFindDto> = runCatchingValidation {
        validate(this) {
            avtaleId?.let { validate(HenteplanFindDto::avtaleId).isPositive() }
            stasjonId?.let { validate(HenteplanFindDto::stasjonId).isPositive() }
            if(before != null && after != null) {
                validate(HenteplanFindDto::after).isGreaterThanStartDateTime(before)
            }
            id?.let { validate(HenteplanFindDto::id).isPositive() }
        }
    }
}