package no.oslokommune.ombruk.partner.form

import kotlinx.serialization.ContextualSerialization
import kotlinx.serialization.Serializable
import no.oslokommune.ombruk.partner.database.PartnerRepository
import no.oslokommune.ombruk.shared.form.IForm
import no.oslokommune.ombruk.shared.utils.validation.isNorwegianPhoneNumber
import no.oslokommune.ombruk.shared.utils.validation.isUniqueInRepository
import no.oslokommune.ombruk.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isEmail
import org.valiktor.functions.isNotBlank
import org.valiktor.validate
import java.time.LocalDateTime

@Serializable
data class PartnerPostForm(
        val navn: String,
        val beskrivelse: String,
        val telefon: String,
        val epost: String
) : IForm<PartnerPostForm> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(PartnerPostForm::navn).isNotBlank().isUniqueInRepository(PartnerRepository)
            validate(PartnerPostForm::telefon).isNorwegianPhoneNumber()
            validate(PartnerPostForm::epost).isEmail()
            validate(PartnerPostForm::beskrivelse).isNotBlank()
        }
    }

}