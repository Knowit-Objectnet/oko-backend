package no.oslokommune.ombruk.partner.form

import kotlinx.serialization.Serializable
import no.oslokommune.ombruk.partner.database.PartnerRepository
import no.oslokommune.ombruk.shared.form.IForm
import no.oslokommune.ombruk.shared.utils.validation.isNorwegianPhoneNumber
import no.oslokommune.ombruk.shared.utils.validation.isUniqueInRepository
import no.oslokommune.ombruk.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isEmail
import org.valiktor.functions.isGreaterThan
import org.valiktor.functions.isNotBlank
import org.valiktor.validate

@Serializable
data class PartnerUpdateForm(
    val id: Int,
    var name: String? = null,
    var description: String? = null,
    var phone: String? = null,
    var email: String? = null
) : IForm<PartnerUpdateForm> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(PartnerUpdateForm::id).isGreaterThan(0)
            validate(PartnerUpdateForm::name).isNotBlank()
            validate(PartnerUpdateForm::description).isNotBlank()
            validate(PartnerUpdateForm::phone).isNotBlank().isNorwegianPhoneNumber()
            validate(PartnerUpdateForm::email).isNotBlank().isEmail()

            PartnerRepository.getPartnerByID(id).map {
                if (it.name != name) validate(PartnerUpdateForm::name).isUniqueInRepository(PartnerRepository)
            }
        }
    }

}