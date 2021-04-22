package ombruk.backend.aktor.application.api.dto

import kotlinx.serialization.Serializable
import ombruk.backend.aktor.domain.model.StasjonUpdateParams
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThan
import org.valiktor.functions.isNotBlank
import org.valiktor.validate

@Serializable
data class StasjonUpdateDto(
    override val id: Int,
    override val navn: String?,
    override val type: Int?
) : IForm<StasjonUpdateDto>, StasjonUpdateParams() {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(StasjonUpdateDto::id).isGreaterThan(0)
            navn?.let{validate(StasjonUpdateDto::navn).isNotBlank()}
//            validate(StationUpdateForm::name).isUniqueInRepository(StationRepository)

        }
    }
}