package ombruk.backend.aktor.application.api.dto

import kotlinx.serialization.Serializable
import ombruk.backend.aktor.domain.model.KontaktUpdateParams
import ombruk.backend.aktor.domain.model.PartnerUpdateParams
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.isNorwegianPhoneNumber
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isEmail
import org.valiktor.functions.isNotBlank
import org.valiktor.validate
import shared.model.serializer.UUIDSerializer
import java.util.*

@Serializable
data class KontaktUpdateDto(
    @Serializable(with = UUIDSerializer::class) override val id: UUID,
    override val navn: String? = null,
    override val telefon: String? = null,
    override val epost: String? = null,
    override val rolle: String? = null
) : IForm<KontaktUpdateDto>, KontaktUpdateParams() {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(KontaktUpdateDto::navn).isNotBlank()
            if (telefon?.isNotBlank() == true) validate(KontaktUpdateDto::telefon).isNorwegianPhoneNumber()
            if (epost?.isNotBlank() == true) validate(KontaktUpdateDto::epost).isEmail()
        }.copy(telefon = telefon?.trim(), epost = epost?.trim(), rolle = rolle?.trim())
    }

}