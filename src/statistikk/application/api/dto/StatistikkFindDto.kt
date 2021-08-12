package ombruk.backend.statistikk.application.api.dto

import io.ktor.locations.*
import kotlinx.serialization.Serializable
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.model.serializer.LocalDateTimeSerializer
import ombruk.backend.shared.utils.validation.runCatchingValidation
import ombruk.backend.statistikk.domain.params.StatistikkFindParams
import org.valiktor.validate
import shared.model.serializer.UUIDSerializer
import java.time.LocalDateTime
import java.util.*

@KtorExperimentalLocationsAPI
@Serializable
@Location("")
data class StatistikkFindDto(
    @Serializable(with = UUIDSerializer::class) override val partnerId: UUID? = null,
    @Serializable(with = UUIDSerializer::class) override val stasjonId: UUID? = null,
    @Serializable(with = UUIDSerializer::class) override val kategoriId: UUID? = null,
    @Serializable(with = LocalDateTimeSerializer::class) override val before: LocalDateTime? = null,
    @Serializable(with = LocalDateTimeSerializer::class) override val after: LocalDateTime? = null
) : IForm<StatistikkFindDto>,
    StatistikkFindParams() {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
        }
    }
}