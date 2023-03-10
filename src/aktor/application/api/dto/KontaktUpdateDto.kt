package ombruk.backend.aktor.application.api.dto

import kotlinx.serialization.Serializable
import ombruk.backend.aktor.domain.model.KontaktUpdateParams
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.isNorwegianPhoneNumberOrBlank
import ombruk.backend.shared.utils.validation.isEmailOrBlank
import ombruk.backend.shared.utils.validation.runCatchingValidation
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
            validate(KontaktUpdateDto::telefon).isNorwegianPhoneNumberOrBlank()
            validate(KontaktUpdateDto::epost).isEmailOrBlank()
        }.copy(telefon = telefon?.trim(), epost = epost?.trim(), rolle = rolle?.trim())
    }

}