package ombruk.backend.kategori.application.api.dto

import io.ktor.locations.*
import kotlinx.serialization.Serializable
import ombruk.backend.kategori.domain.params.KategoriFindParams
import ombruk.backend.kategori.domain.params.KategoriUpdateParams
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isNotBlank
import org.valiktor.validate
import shared.model.serializer.UUIDSerializer
import java.util.*

@Serializable
data class KategoriUpdateDto(
    @Serializable(with = UUIDSerializer::class) override val id: UUID,
    override val navn: String? = null, override val vektkategori: Boolean?
) : IForm<KategoriUpdateDto>, KategoriUpdateParams(){
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            navn?.let { validate(KategoriUpdateDto::navn).isNotBlank() }
        }
    }
}