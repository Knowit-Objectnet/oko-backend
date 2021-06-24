package ombruk.backend.kategori.application.api.dto

import arrow.core.Either
import io.ktor.locations.*
import kotlinx.serialization.Serializable
import ombruk.backend.kategori.domain.params.EkstraHentingKategoriFindParams
import ombruk.backend.kategori.domain.params.HenteplanKategoriFindParams
import ombruk.backend.shared.error.ValidationError
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isPositiveOrZero
import org.valiktor.validate
import shared.model.serializer.UUIDSerializer
import java.util.*

@KtorExperimentalLocationsAPI
@Location("/{ekstraHentingId}/kategorier")
@Serializable
data class EkstraHentingKategoriFindDto(
    @Serializable(with = UUIDSerializer::class) override val id: UUID? = null,
    @Serializable(with = UUIDSerializer::class) override var ekstraHentingId: UUID? = null,
    @Serializable(with = UUIDSerializer::class) override val kategoriId: UUID? = null,
    override val mengde: Float? = null
) : IForm<EkstraHentingKategoriFindDto>,
    EkstraHentingKategoriFindParams() {
    override fun validOrError(): Either<ValidationError, EkstraHentingKategoriFindDto> = runCatchingValidation {
        validate(this) {
            mengde.let {
                validate(EkstraHentingKategoriFindDto::mengde).isPositiveOrZero()
            }
        }
    }
}