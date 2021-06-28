package ombruk.backend.partner.form

import kotlinx.serialization.Serializable
import ombruk.backend.partner.database.PartnerRepository
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.isNorwegianPhoneNumberOrBlank
import ombruk.backend.shared.utils.validation.isUniqueInRepository
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isEmail
import org.valiktor.functions.isNotBlank
import org.valiktor.validate

@Serializable
data class PartnerPostForm(
    var name: String,
    var description: String? = null,
    var phone: String? = null,
    var email: String? = null
) : IForm<PartnerPostForm> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(PartnerPostForm::name).isNotBlank().isUniqueInRepository(PartnerRepository)
            validate(PartnerPostForm::phone).isNorwegianPhoneNumberOrBlank()
            validate(PartnerPostForm::email).isEmail()
            validate(PartnerPostForm::description).isNotBlank()
        }
    }

}