package ombruk.backend.henting.application.api.dto

import arrow.core.Either
import ombruk.backend.shared.error.ValidationError
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.validate
import java.time.LocalDate

//Is never passed over net, and therefore does not need to be serialized
data class PlanlagtHentingBatchPostDto(
    val postDto: PlanlagtHentingPostDto,
    val dateList: List<LocalDate>
) : IForm<PlanlagtHentingBatchPostDto> {
    override fun validOrError(): Either<ValidationError, PlanlagtHentingBatchPostDto> = runCatchingValidation {
        validate(this) {
        }
    }
}