package ombruk.backend.kategori.application.api.dto

import arrow.core.Either
import kotlinx.serialization.Serializable
import ombruk.backend.kategori.domain.params.HenteplanKategoriCreateParams
import ombruk.backend.shared.error.ValidationError
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.validate
import shared.model.serializer.UUIDSerializer
import java.util.*

@Serializable
data class HenteplanKategoriSaveDto(
    //@Serializable(with = UUIDSerializer::class) override val id: UUID,
    @Serializable(with = UUIDSerializer::class) override val henteplanId: UUID,
    @Serializable(with = UUIDSerializer::class) override val kategoriId: UUID,
    override val merknad: String? = null
) : IForm<HenteplanKategoriSaveDto>, HenteplanKategoriCreateParams() {
    override fun validOrError(): Either<ValidationError, HenteplanKategoriSaveDto> = runCatchingValidation {
        validate(this) {}
    }
}