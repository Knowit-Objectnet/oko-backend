package ombruk.backend.utlysning.application.api.dto

import io.ktor.locations.*
import kotlinx.serialization.Serializable
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.validate
import shared.model.serializer.UUIDSerializer
import java.util.*

@KtorExperimentalLocationsAPI
@Location("/{id}")
@Serializable
data class UtlysningFindOneDto(
    @Serializable(with = UUIDSerializer::class) val id: UUID
) : IForm<UtlysningFindOneDto> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
        }
    }
}