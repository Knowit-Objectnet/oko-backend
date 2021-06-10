package ombruk.backend.aktor.application.api.dto

import io.ktor.locations.*
import ombruk.backend.aktor.domain.model.KontaktFindParams
import ombruk.backend.aktor.domain.model.PartnerFindParams
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isNotBlank
import org.valiktor.validate
import java.util.*

@KtorExperimentalLocationsAPI
@Location("")
data class KontaktGetDto(
    override val id: UUID? = null,
    override val aktorId: UUID? = null,
    override val navn: String? = null,
    override val telefon: String? = null,
    override val epost: String? = null,
    override val rolle: String? = null
) : IForm<KontaktGetDto>, KontaktFindParams() {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            navn?.let { validate(KontaktGetDto::navn).isNotBlank() }
//            telefon?.let { validate(PartnerGetDto::telefon).isNorwegianPhoneNumber() }
            //FIXME: Validate UUID?
        }

    }
}