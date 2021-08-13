package ombruk.backend.kategori.application.api.dto

import io.ktor.locations.*
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.validate
import java.util.*

@KtorExperimentalLocationsAPI
@Location("/{id}")
data class KategoriFindOneDto(
    val id: UUID
) : IForm<KategoriFindOneDto> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
        }
    }
}