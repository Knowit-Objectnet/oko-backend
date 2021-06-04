package ombruk.backend.aktor.application.api.dto

import kotlinx.serialization.Serializable
import ombruk.backend.aktor.domain.model.PartnerCreateParams
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isNotBlank
import org.valiktor.validate

@Serializable
data class PartnerSaveDto(
    override val navn: String,
    override val ideell: Boolean
) : IForm<PartnerSaveDto>, PartnerCreateParams() {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(PartnerSaveDto::navn).isNotBlank() //.isUniqueInRepository(PartnerRepository)
            //FIXME: Validate UUID?
        }
    }

}