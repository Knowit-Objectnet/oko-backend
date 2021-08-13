package ombruk.backend.kategori.application.api.dto

import arrow.core.Either
import io.ktor.locations.*
import kotlinx.serialization.Serializable
import ombruk.backend.kategori.domain.params.HenteplanKategoriFindParams
import ombruk.backend.shared.error.ValidationError
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.validate
import shared.model.serializer.UUIDSerializer
import java.util.*

@KtorExperimentalLocationsAPI
@Location("/{henteplanId}/kategorier")
@Serializable
data class HenteplanKategoriFindDto(
    @Serializable(with = UUIDSerializer::class) override val id: UUID? = null,
    @Serializable(with = UUIDSerializer::class) override var henteplanId: UUID? = null,
    @Serializable(with = UUIDSerializer::class) override val kategoriId: UUID? = null,
) : IForm<HenteplanKategoriFindDto>,
    HenteplanKategoriFindParams() {
    override fun validOrError(): Either<ValidationError, HenteplanKategoriFindDto> = runCatchingValidation {
        validate(this) {
        }
    }
}