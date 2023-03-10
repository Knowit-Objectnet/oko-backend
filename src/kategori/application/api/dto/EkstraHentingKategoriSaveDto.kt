package ombruk.backend.kategori.application.api.dto

import arrow.core.Either
import kotlinx.serialization.Serializable
import ombruk.backend.kategori.domain.params.EkstraHentingKategoriCreateParams
import ombruk.backend.kategori.domain.params.HenteplanKategoriCreateParams
import ombruk.backend.shared.error.ValidationError
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isPositiveOrZero
import org.valiktor.validate
import shared.model.serializer.UUIDSerializer
import java.util.*

@Serializable
data class EkstraHentingKategoriSaveDto(
    @Serializable(with = UUIDSerializer::class) override val ekstraHentingId: UUID,
    @Serializable(with = UUIDSerializer::class) override val kategoriId: UUID,
    override val mengde: Float? = null
) : IForm<EkstraHentingKategoriSaveDto>, EkstraHentingKategoriCreateParams() {
    override fun validOrError(): Either<ValidationError, EkstraHentingKategoriSaveDto> = runCatchingValidation {
        validate(this) {
            mengde.let {
                validate(EkstraHentingKategoriSaveDto::mengde).isPositiveOrZero()
            }
        }
    }
}