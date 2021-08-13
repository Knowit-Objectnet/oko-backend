package ombruk.backend.vektregistrering.application.api.dto

import arrow.core.Either
import io.ktor.locations.*
import kotlinx.serialization.Serializable
import ombruk.backend.kategori.domain.params.HenteplanKategoriFindParams
import ombruk.backend.shared.error.ValidationError
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.model.serializer.LocalDateTimeSerializer
import ombruk.backend.shared.utils.validation.runCatchingValidation
import ombruk.backend.vektregistrering.domain.params.VektregistreringFindParams
import org.valiktor.validate
import shared.model.serializer.UUIDSerializer
import java.time.LocalDateTime
import java.util.*

@KtorExperimentalLocationsAPI
@Location("/{id}")
@Serializable
data class VektregistreringFindOneDto(@Serializable(with = UUIDSerializer::class) val id: UUID) : IForm<VektregistreringFindOneDto> {
    override fun validOrError(): Either<ValidationError, VektregistreringFindOneDto> = runCatchingValidation {
        validate(this) {
        }
    }
}