package ombruk.backend.utlysning.application.api.dto

import arrow.core.Either
import io.ktor.locations.*
import kotlinx.serialization.Serializable
import ombruk.backend.shared.error.ValidationError
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.validate
import shared.model.serializer.UUIDSerializer
import java.util.*

@Serializable
@Location("/{id}")
data class UtlysningDeleteDto(@Serializable(with = UUIDSerializer::class) val id: UUID) : IForm<UtlysningDeleteDto> {
    override fun validOrError(): Either<ValidationError, UtlysningDeleteDto> = runCatchingValidation {
        validate(this) {
        }
    }
}