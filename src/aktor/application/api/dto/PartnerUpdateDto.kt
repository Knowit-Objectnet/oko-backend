package ombruk.backend.aktor.application.api.dto

import kotlinx.serialization.Serializable
import ombruk.backend.aktor.domain.enum.PartnerStorrelse
import ombruk.backend.aktor.domain.model.PartnerUpdateParams
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.isNorwegianPhoneNumber
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isEmail
import org.valiktor.functions.isGreaterThan
import org.valiktor.functions.isNotBlank
import org.valiktor.validate

@Serializable
data class PartnerUpdateDto(
    override val id: Int,
    override val navn: String? = null,
    override val ideell: Boolean? = null,
    override val storrelse: PartnerStorrelse? = null
//    val id: Int,
//    var navn: String? = null,
//    var beskrivelse: String? = null,
//    var telefon: String? = null,
//    var epost: String? = null
) : IForm<PartnerUpdateDto>, PartnerUpdateParams() {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(PartnerUpdateDto::id).isGreaterThan(0)
            validate(PartnerUpdateDto::navn).isNotBlank()
//            validate(PartnerUpdateDto::beskrivelse).isNotBlank()
//            validate(PartnerUpdateDto::telefon).isNotBlank().isNorwegianPhoneNumber()
//            validate(PartnerUpdateDto::epost).isNotBlank().isEmail()

//            PartnerRepository.getPartnerByID(id).map {
//                if (it.navn != navn) validate(PartnerUpdateForm::navn).isUniqueInRepository(PartnerRepository)
//            }
        }
    }

}