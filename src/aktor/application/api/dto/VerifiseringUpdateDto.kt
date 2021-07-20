package ombruk.backend.aktor.application.api.dto

import kotlinx.serialization.Serializable
import ombruk.backend.aktor.domain.model.KontaktUpdateParams
import ombruk.backend.aktor.domain.model.VerifiseringUpdateParams
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.isNorwegianPhoneNumberOrBlank
import ombruk.backend.shared.utils.validation.isEmailOrBlank
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isNotBlank
import org.valiktor.validate
import shared.model.serializer.UUIDSerializer
import java.util.*

@Serializable
data class VerifiseringUpdateDto(
    @Serializable(with = UUIDSerializer::class) override val id: UUID,
    override val telefonKode: String? = null,
    override val telefonVerifisert: Boolean? = null,
    override val epostKode: String? = null,
    override val epostVerifisert: Boolean? = null,
    override val resetTelefon: Boolean = false,
    override val resetEpost: Boolean = false
) : IForm<VerifiseringUpdateDto>, VerifiseringUpdateParams() {
    override fun validOrError() = runCatchingValidation {
        validate(this) {}
    }

}