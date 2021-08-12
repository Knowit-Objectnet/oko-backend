package ombruk.backend.kategori.application.api.dto

import io.ktor.locations.*
import ombruk.backend.kategori.domain.params.KategoriFindParams
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isNotBlank
import org.valiktor.validate
import java.util.*

@KtorExperimentalLocationsAPI
@Location("")
data class KategoriFindDto(
    override val id: UUID? = null,
    override val navn: String? = null,
    override val vektkategori: Boolean? = null
) : IForm<KategoriFindDto>,
    KategoriFindParams() {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            navn?.let { validate(KategoriFindDto::navn).isNotBlank() }
        }
    }
}