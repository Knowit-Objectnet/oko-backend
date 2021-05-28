package ombruk.backend.aktor.application.api.dto

import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import ombruk.backend.aktor.domain.enum.PartnerStorrelse
import ombruk.backend.aktor.domain.model.PartnerFindParams
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isNotBlank
import org.valiktor.validate
import java.util.*

@KtorExperimentalLocationsAPI
@Location("")
data class PartnerGetDto(
    override val navn: String? = null,
    override val storrelse: PartnerStorrelse? = null,
    override val ideell: Boolean? = null,
    override val id: UUID? = null
) : IForm<PartnerGetDto>, PartnerFindParams() {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            navn?.let { validate(PartnerGetDto::navn).isNotBlank() }
//            telefon?.let { validate(PartnerGetDto::telefon).isNorwegianPhoneNumber() }
            //FIXME: Validate UUID?
        }

    }
}