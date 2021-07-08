package ombruk.backend.aarsak.application.api.dto

import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import ombruk.backend.aarsak.domain.enum.AarsakType
import ombruk.backend.aarsak.domain.model.AarsakFindParams
import ombruk.backend.aktor.domain.enum.StasjonType
import ombruk.backend.aktor.domain.model.StasjonFindParams
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isNotBlank
import org.valiktor.validate
import java.util.*

@KtorExperimentalLocationsAPI
@Location("")
data class AarsakFindDto(
    override val id: UUID?,
    override val beskrivelse: String? = null,
    override val type: AarsakType?
) : IForm<AarsakFindDto>,
    AarsakFindParams() {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            beskrivelse?.let { validate(AarsakFindDto::beskrivelse).isNotBlank() }
            //FIXME: Validate UUID?
        }
    }
}