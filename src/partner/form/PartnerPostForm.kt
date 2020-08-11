package ombruk.backend.partner.form

import kotlinx.serialization.Serializable
import ombruk.backend.partner.database.PartnerRepository
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.isNorwegianPhoneNumber
import ombruk.backend.shared.utils.validation.isPartnerUnique
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
            if (name.isNotBlank()) validate(PartnerPostForm::name).isPartnerUnique(PartnerRepository)
            validate(PartnerPostForm::name).isNotBlank().isPartnerUnique(PartnerRepository)
            description?.let { validate(PartnerPostForm::phone).isNotBlank().isNorwegianPhoneNumber() }
            email?.let { validate(PartnerPostForm::email).isNotBlank().isEmail() }
            description?.let { validate(PartnerPostForm::description).isNotBlank() }
        }
    }

}