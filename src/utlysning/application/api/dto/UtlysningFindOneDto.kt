package ombruk.backend.utlysning.application.api.dto

import io.ktor.locations.*
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.validate
import java.util.*

@KtorExperimentalLocationsAPI
@Location("/{id}")
data class UtlysningFindOneDto(
    val id: UUID
) : IForm<UtlysningFindOneDto> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
        }
    }
}