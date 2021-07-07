package ombruk.backend.henting.application.api.dto

import arrow.core.Either
import io.ktor.locations.*
import kotlinx.serialization.Serializable
import ombruk.backend.henting.domain.params.EkstraHentingFindParams
import ombruk.backend.shared.error.ValidationError
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.model.serializer.LocalDateTimeSerializer
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThanOrEqualTo
import org.valiktor.validate
import shared.model.serializer.UUIDSerializer
import java.time.LocalDateTime
import java.util.*

@KtorExperimentalLocationsAPI
@Serializable
@Location("")
data class EkstraHentingFindDto(
    @Serializable(with = UUIDSerializer::class) override val id: UUID? = null,
    @Serializable(with = LocalDateTimeSerializer::class) override val before: LocalDateTime? = null,
    @Serializable(with = LocalDateTimeSerializer::class) override val after: LocalDateTime? = null,
    override val beskrivelse: String? = null,
    @Serializable(with = UUIDSerializer::class) override val stasjonId: UUID? = null,
) : IForm<EkstraHentingFindDto>, EkstraHentingFindParams() {
    override fun validOrError(): Either<ValidationError, EkstraHentingFindDto> = runCatchingValidation {
        validate(this) {
            if(before != null && after != null) {
                validate(EkstraHentingFindDto::before).isGreaterThanOrEqualTo(after)
            }
        }
    }
}