package ombruk.backend.avtale.application.api.dto

import arrow.core.Either
import io.ktor.locations.*
import kotlinx.serialization.Serializable
import ombruk.backend.shared.error.ValidationError
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isPositive
import org.valiktor.validate

@Serializable
@Location("/{id}")
data class AvtaleDeleteDto(val id: Int) : IForm<AvtaleDeleteDto> {
    override fun validOrError(): Either<ValidationError, AvtaleDeleteDto> = runCatchingValidation {
        validate(this) {
            validate(AvtaleDeleteDto::id).isPositive()
        }
    }
}