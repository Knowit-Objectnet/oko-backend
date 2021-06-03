package ombruk.backend.aktor.application.api.dto

import kotlinx.serialization.Serializable
import ombruk.backend.aktor.domain.enum.StasjonType
import ombruk.backend.aktor.domain.model.StasjonCreateParams
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isNotBlank
import org.valiktor.validate

@Serializable
data class StasjonSaveDto(
    override val navn: String,
    override val type: StasjonType
) : IForm<StasjonSaveDto>, StasjonCreateParams() {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(StasjonSaveDto::navn).isNotBlank()
//            validate(StasjonCreateDto::name).isUniqueInRepository(StationRepository)
            //FIXME: Validate UUID?
        }
    }
}