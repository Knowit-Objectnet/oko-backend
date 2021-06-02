package ombruk.backend.utlysning.application.api.dto

import arrow.core.Either
import kotlinx.serialization.Serializable
import ombruk.backend.shared.error.ValidationError
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.model.serializer.LocalDateTimeSerializer
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.validate
import shared.model.serializer.UUIDSerializer
import java.time.LocalDateTime
import java.util.*

data class UtlysningBatchPostDto(
    val hentingId: UUID,
    val partnerPameldt: LocalDateTime? = null,
    val stasjonGodkjent: LocalDateTime? = null,
    val partnerSkjult: Boolean = false,
    val partnerVist: Boolean = false,
    val partnerIds: List<UUID>
) : IForm<UtlysningBatchPostDto> {
    override fun validOrError(): Either<ValidationError, UtlysningBatchPostDto> = runCatchingValidation {
        validate(this) {
        }
    }
}
