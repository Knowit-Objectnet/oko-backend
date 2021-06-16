package ombruk.backend.utlysning.application.api.dto

import io.ktor.locations.*
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.runCatchingValidation
import ombruk.backend.utlysning.domain.params.UtlysningFindParams
import org.valiktor.functions.isNotBlank
import org.valiktor.validate
import java.time.LocalDateTime
import java.util.*

@KtorExperimentalLocationsAPI
@Location("")
data class UtlysningFindDto(
    override val id: UUID? = null,
    override val partnerId: UUID? = null,
    override val hentingId: UUID? = null,
    override val partnerPameldt: Boolean? = null,
    override val stasjonGodkjent: Boolean? = null,
    override val partnerSkjult: Boolean? = null,
    override val partnerVist: Boolean? = null,
) : IForm<UtlysningFindDto>,
    UtlysningFindParams() {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
        }
    }
}