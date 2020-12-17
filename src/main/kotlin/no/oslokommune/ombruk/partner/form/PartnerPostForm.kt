package no.oslokommune.ombruk.partner.form

import io.swagger.v3.oas.annotations.media.Schema
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
    @field:Schema(required = true, example = "Fretex", nullable = false) val navn: String,
    @field:Schema(required = true, example = "Fretex driver med gjenbruk", nullable = false) val beskrivelse: String,
    @field:Schema(required = true, example = "12345678", nullable = false) val telefon: String,
    @field:Schema(required = true, example = "Fretex@Fretex.no", nullable = false) val epost: String
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