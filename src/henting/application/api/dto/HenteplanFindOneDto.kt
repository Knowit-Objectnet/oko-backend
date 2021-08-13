package ombruk.backend.henting.application.api.dto

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

@KtorExperimentalLocationsAPI
@Location("/{id}")
@Serializable
data class HenteplanFindOneDto(@Serializable(with = UUIDSerializer::class) val id: UUID): IForm<HenteplanFindOneDto> {
    override fun validOrError(): Either<ValidationError, HenteplanFindOneDto>  = runCatchingValidation{
        validate(this) {
        }
    }
}