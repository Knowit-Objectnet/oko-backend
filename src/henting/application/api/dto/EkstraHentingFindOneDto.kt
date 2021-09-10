package ombruk.backend.henting.application.api.dto

import arrow.core.Either
import io.ktor.locations.*
import kotlinx.serialization.Serializable
import ombruk.backend.shared.error.ValidationError
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.validate
import shared.model.serializer.UUIDSerializer
import java.util.*

@KtorExperimentalLocationsAPI
@Location("/{id}")
@Serializable
data class EkstraHentingFindOneDto(
    @Serializable(with = UUIDSerializer::class) val id: UUID,
    @Serializable(with = UUIDSerializer::class) val stasjonId: UUID? = null,
    ): IForm<EkstraHentingFindOneDto> {
    override fun validOrError(): Either<ValidationError, EkstraHentingFindOneDto>  = runCatchingValidation{
        validate(this) {
        }
    }
}