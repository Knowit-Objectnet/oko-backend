package ombruk.backend.henting.application.api.dto

import arrow.core.Either
import kotlinx.serialization.Serializable
import ombruk.backend.henting.domain.params.PlanlagtHentingCreateParams
import ombruk.backend.shared.error.ValidationError
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.model.serializer.LocalDateTimeSerializer
import ombruk.backend.shared.utils.validation.isGreaterThanStartDateTime
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.validate
import shared.model.serializer.UUIDSerializer
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Serializable(with = UUIDSerializer::class)
data class PlanlagtHentingBatchPostDto(
    val postDto: PlanlagtHentingPostDto,
    @Serializable(with = LocalDateTimeSerializer::class) val dateList: List<LocalDate>
) : IForm<PlanlagtHentingBatchPostDto> {
    override fun validOrError(): Either<ValidationError, PlanlagtHentingBatchPostDto> = runCatchingValidation {
        validate(this) {
        }
    }
}