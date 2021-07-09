package ombruk.backend.kategori.application.api.dto

import arrow.core.Either
import kotlinx.serialization.Serializable
import ombruk.backend.kategori.domain.params.KategoriCreateParams
import ombruk.backend.shared.error.ValidationError
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.validate

@Serializable
data class KategoriSaveDto(
    override val navn: String,
    override val vektkategori: Boolean? = false
) : IForm<KategoriSaveDto>, KategoriCreateParams() {
    override fun validOrError(): Either<ValidationError, KategoriSaveDto> = runCatchingValidation {
        validate(this) {}
    }
}