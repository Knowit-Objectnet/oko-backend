package no.oslokommune.ombruk.partner.form

import kotlinx.serialization.Serializable
import no.oslokommune.ombruk.partner.database.PartnerRepository
import no.oslokommune.ombruk.shared.form.IForm
import no.oslokommune.ombruk.shared.utils.validation.isNorwegianPhoneNumber
import no.oslokommune.ombruk.shared.utils.validation.isUniqueInRepository
import no.oslokommune.ombruk.shared.utils.validation.runCatchingValidation
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
            validate(PartnerPostForm::phone).isNorwegianPhoneNumber()
            validate(PartnerPostForm::email).isEmail()
            validate(PartnerPostForm::description).isNotBlank()
        }
    }

}