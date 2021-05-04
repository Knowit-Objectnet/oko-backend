package ombruk.backend.henting.application.api.dto

import arrow.core.Either
import io.ktor.locations.*
import kotlinx.serialization.Serializable
import ombruk.backend.shared.error.ValidationError
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isPositive
import org.valiktor.validate

@KtorExperimentalLocationsAPI
@Serializable
@Location("/{id}")
data class HenteplanDeleteDto(val id: Int): IForm<HenteplanDeleteDto> {
    override fun validOrError(): Either<ValidationError, HenteplanDeleteDto> = runCatchingValidation{
        validate(this) {
            validate(HenteplanDeleteDto::id).isPositive()
        }
    }
}