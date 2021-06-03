package ombruk.backend.aktor.application.api.dto

import kotlinx.serialization.Serializable
import ombruk.backend.aktor.domain.model.PartnerUpdateParams
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isNotBlank
import org.valiktor.validate
import shared.model.serializer.UUIDSerializer
import java.util.*

@Serializable
data class PartnerUpdateDto(
    @Serializable(with = UUIDSerializer::class) override val id: UUID,
    override val navn: String? = null,
    override val ideell: Boolean? = null,
) : IForm<PartnerUpdateDto>, PartnerUpdateParams() {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(PartnerUpdateDto::navn).isNotBlank()
            //FIXME: Validate UUID?
        }
    }

}