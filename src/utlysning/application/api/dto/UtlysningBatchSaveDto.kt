package ombruk.backend.utlysning.application.api.dto

import arrow.core.Either
import kotlinx.serialization.Serializable
import ombruk.backend.shared.error.ValidationError
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.model.serializer.LocalDateTimeSerializer
import ombruk.backend.shared.utils.validation.allUUIDLegal
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.validate
import shared.model.serializer.UUIDSerializer
import java.time.LocalDateTime
import java.util.*

@Serializable
data class UtlysningBatchSaveDto(
    @Serializable(with = UUIDSerializer::class) val hentingId: UUID,
    @Serializable(with = LocalDateTimeSerializer::class) val partnerPameldt: LocalDateTime? = null,
    @Serializable(with = LocalDateTimeSerializer::class) val stasjonGodkjent: LocalDateTime? = null,
    val partnerSkjult: Boolean = false,
    val partnerVist: Boolean = false,
    val partnerIds: List<String>//Serializer library does not allow for serializing list of UUID
) : IForm<UtlysningBatchSaveDto> {
    override fun validOrError(): Either<ValidationError, UtlysningBatchSaveDto> = runCatchingValidation {
        validate(this) {
            validate(UtlysningBatchSaveDto::partnerIds).allUUIDLegal()
        }
    }
}
