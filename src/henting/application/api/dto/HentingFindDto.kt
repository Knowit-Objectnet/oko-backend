package ombruk.backend.henting.application.api.dto

import arrow.core.Either
import io.ktor.locations.*
import kotlinx.serialization.Serializable
import ombruk.backend.henting.domain.model.HenteplanFrekvens
import ombruk.backend.henting.domain.params.EkstraHentingFindParams
import ombruk.backend.henting.domain.params.HenteplanFindParams
import ombruk.backend.henting.domain.params.HentingWrapperFindParams
import ombruk.backend.shared.error.ValidationError
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.model.serializer.LocalDateTimeSerializer
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThanOrEqualTo
import org.valiktor.validate
import shared.model.serializer.UUIDSerializer
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.util.*

@Serializable
@Location("")
data class HentingFindDto(
    @Serializable(with = UUIDSerializer::class) override val id: UUID? = null,
    @Serializable(with = UUIDSerializer::class) override val stasjonId: UUID? = null,
    @Serializable(with = UUIDSerializer::class) override val aktorId: UUID? = null,
    @Serializable(with = LocalDateTimeSerializer::class) override val after: LocalDateTime? = null,
    @Serializable(with = LocalDateTimeSerializer::class) override val before: LocalDateTime? = null,
    ): IForm<HentingFindDto>, HentingWrapperFindParams() {
    override fun validOrError(): Either<ValidationError, HentingFindDto>  = runCatchingValidation{
        validate(this) {
        }
    }
}