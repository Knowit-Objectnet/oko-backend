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
@Location("/{id}")
@Serializable
data class HenteplanFindOneDto(val id: Int): IForm<HenteplanFindOneDto> {
    override fun validOrError(): Either<ValidationError, HenteplanFindOneDto>  = runCatchingValidation{
        validate(this) {
            validate(HenteplanFindOneDto::id).isPositive()
        }
    }
}