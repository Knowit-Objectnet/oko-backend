package no.oslokommune.ombruk.partner.form

import kotlinx.serialization.Serializable
import no.oslokommune.ombruk.partner.database.SamPartnerRepository
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
        var navn: String? = null,
        var beskrivelse: String? = null,
        var telefon: String? = null,
        var epost: String? = null
) : IForm<PartnerUpdateForm> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(PartnerUpdateForm::id).isGreaterThan(0)
            validate(PartnerUpdateForm::navn).isNotBlank()
            validate(PartnerUpdateForm::beskrivelse).isNotBlank()
            validate(PartnerUpdateForm::telefon).isNotBlank().isNorwegianPhoneNumber()
            validate(PartnerUpdateForm::epost).isNotBlank().isEmail()

            SamPartnerRepository.getPartnerByID(id).map {
                if (it.navn != navn) validate(PartnerUpdateForm::navn).isUniqueInRepository(SamPartnerRepository)
            }
        }
    }

}