package ombruk.backend.avtale.application.api.dto

import arrow.core.Either
import io.ktor.locations.*
import kotlinx.serialization.Serializable
import ombruk.backend.shared.error.ValidationError
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isPositive
import org.valiktor.validate
import shared.model.serializer.UUIDSerializer
import java.util.*

@Serializable
@Location("/{id}")
data class AvtaleDeleteDto(@Serializable(with = UUIDSerializer::class) val id: UUID) : IForm<AvtaleDeleteDto> {
    override fun validOrError(): Either<ValidationError, AvtaleDeleteDto> = runCatchingValidation {
        validate(this) {
        }
    }
}