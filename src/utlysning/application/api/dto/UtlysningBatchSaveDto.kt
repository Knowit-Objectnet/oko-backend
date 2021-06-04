package ombruk.backend.utlysning.application.api.dto

import arrow.core.Either
import ombruk.backend.shared.error.ValidationError
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.validate
import java.time.LocalDateTime
import java.util.*

data class UtlysningBatchSaveDto(
    val hentingId: UUID,
    val partnerPameldt: LocalDateTime? = null,
    val stasjonGodkjent: LocalDateTime? = null,
    val partnerSkjult: Boolean = false,
    val partnerVist: Boolean = false,
    val partnerIds: List<UUID>
) : IForm<UtlysningBatchSaveDto> {
    override fun validOrError(): Either<ValidationError, UtlysningBatchSaveDto> = runCatchingValidation {
        validate(this) {
        }
    }
}
