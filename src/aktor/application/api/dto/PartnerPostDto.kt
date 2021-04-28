package ombruk.backend.aktor.application.api.dto

import kotlinx.serialization.Serializable
import ombruk.backend.aktor.domain.enum.PartnerStorrelse
import ombruk.backend.aktor.domain.model.PartnerCreateParams
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isNotBlank
import org.valiktor.validate

@Serializable
data class PartnerPostDto(
    override val navn: String,
    override val storrelse: PartnerStorrelse,
    override val ideell: Boolean
) : IForm<PartnerPostDto>, PartnerCreateParams() {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(PartnerPostDto::navn).isNotBlank() //.isUniqueInRepository(PartnerRepository)
        }
    }

}