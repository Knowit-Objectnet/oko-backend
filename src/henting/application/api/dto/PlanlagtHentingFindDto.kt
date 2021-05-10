package ombruk.backend.henting.application.api.dto

import arrow.core.Either
import io.ktor.locations.*
import kotlinx.serialization.Serializable
import ombruk.backend.henting.domain.params.PlanlagtHentingFindParams
import ombruk.backend.shared.error.ValidationError
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.model.serializer.LocalDateTimeSerializer
import ombruk.backend.shared.utils.validation.isGreaterThanStartDateTime
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.validate
import shared.model.serializer.UUIDSerializer
import java.time.LocalDateTime
import java.util.*

@KtorExperimentalLocationsAPI
@Serializable(with = UUIDSerializer::class)
@Location("/")
data class PlanlagtHentingFindDto(
    override val id: UUID? = null,
    @Serializable(with = LocalDateTimeSerializer::class) override val startTidspunkt: LocalDateTime? = null,
    @Serializable(with = LocalDateTimeSerializer::class) override val sluttTidspunkt: LocalDateTime? = null,
    override val merknad: String? = null,
    override val henteplanId: UUID? = null,
    @Serializable(with = LocalDateTimeSerializer::class) override val avlyst: LocalDateTime? = null
) : IForm<PlanlagtHentingFindDto>, PlanlagtHentingFindParams() {
    override fun validOrError(): Either<ValidationError, PlanlagtHentingFindDto> = runCatchingValidation {
        validate(this) {
            if(startTidspunkt != null && sluttTidspunkt != null) {
                validate(PlanlagtHentingFindDto::sluttTidspunkt).isGreaterThanStartDateTime(startTidspunkt)
            }
        }
    }
}