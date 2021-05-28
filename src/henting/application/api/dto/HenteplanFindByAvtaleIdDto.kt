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
@Location("/avtale/{avtaleId}")
@Serializable
data class HenteplanFindByAvtaleIdDto(@Serializable(with = UUIDSerializer::class) val avtaleId: UUID): IForm<HenteplanFindByAvtaleIdDto> {
    override fun validOrError(): Either<ValidationError, HenteplanFindByAvtaleIdDto>  = runCatchingValidation{
        validate(this) {
        }
    }
}