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
    var description: String,
    var phone: String,
    var email: String
) : IForm<PartnerPostForm> {
    override fun validOrError()= runCatchingValidation {
        validate(this){
            validate(PartnerPostForm::name).isNotBlank().isPartnerUnique(PartnerRepository)
            validate(PartnerPostForm::phone).isNotBlank().isNorwegianPhoneNumber()
            validate(PartnerPostForm::email).isNotBlank().isEmail()
            validate(PartnerPostForm::description).isNotBlank()
        }
    }

}