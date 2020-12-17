package no.oslokommune.ombruk.partner.form

import io.swagger.v3.oas.annotations.media.Schema
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
    @field:Schema(required = true, example = "1") val id: Int,
    @field:Schema(required = false, example = "Jobben") var navn: String? = null,
    @field:Schema(required = false, example = "Jobben driver med en annen ting") var beskrivelse: String? = null,
    @field:Schema(required = false, example = "87654321") var telefon: String? = null,
    @field:Schema(required = false, example = "Jobben@Jobben.no") var epost: String? = null
) : IForm<PartnerUpdateForm> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(PartnerUpdateForm::id).isGreaterThan(0)
            validate(PartnerUpdateForm::navn).isNotBlank()
            validate(PartnerUpdateForm::beskrivelse).isNotBlank()
            validate(PartnerUpdateForm::telefon).isNotBlank().isNorwegianPhoneNumber()
            validate(PartnerUpdateForm::epost).isNotBlank().isEmail()

            PartnerRepository.getPartnerByID(id).map {
                if (it.navn != navn) validate(PartnerUpdateForm::navn).isUniqueInRepository(PartnerRepository)
            }
        }
    }

}