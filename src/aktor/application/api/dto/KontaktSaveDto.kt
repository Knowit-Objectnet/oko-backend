package ombruk.backend.aktor.application.api.dto

import kotlinx.serialization.Serializable
import ombruk.backend.aktor.domain.model.KontaktCreateParams
import ombruk.backend.aktor.domain.model.PartnerCreateParams
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isNotBlank
import org.valiktor.validate
import shared.model.serializer.UUIDSerializer
import java.util.*

@Serializable
data class KontaktSaveDto(
    @Serializable(with = UUIDSerializer::class) override val aktorId: UUID,
    override val navn: String,
    override val telefon: String?,
    override val epost: String?,
    override val rolle: String?,
) : IForm<KontaktSaveDto>, KontaktCreateParams() {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(KontaktSaveDto::navn).isNotBlank()
        }
    }

}