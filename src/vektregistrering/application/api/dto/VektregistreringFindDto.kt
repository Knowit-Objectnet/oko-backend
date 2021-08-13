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
@Location("")
@Serializable
data class VektregistreringFindDto(
    @Serializable(with = UUIDSerializer::class) override val id: UUID? = null,
    @Serializable(with = UUIDSerializer::class) override var hentingId: UUID? = null,
    @Serializable(with = UUIDSerializer::class) override val kategoriId: UUID? = null,
    override val vekt: Float? = null,
    @Serializable(with = LocalDateTimeSerializer::class) override val before: LocalDateTime? = null,
    @Serializable(with = LocalDateTimeSerializer::class) override val after: LocalDateTime? = null,
    ) : IForm<VektregistreringFindDto>,
    VektregistreringFindParams() {
    override fun validOrError(): Either<ValidationError, VektregistreringFindDto> = runCatchingValidation {
        validate(this) {
        }
    }
}