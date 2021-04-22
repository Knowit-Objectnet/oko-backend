package ombruk.backend.aktor.application.api.dto

import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import ombruk.backend.aktor.domain.enum.StasjonType
import ombruk.backend.aktor.domain.model.StasjonFindParams
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThan
import org.valiktor.functions.isNotBlank
import org.valiktor.validate

@KtorExperimentalLocationsAPI
@Location("/")
data class StasjonFindDto(
    override val navn: String? = null,
    override val type: StasjonType? = null,
    override val id: Int? = null
) : IForm<StasjonFindDto>,
    StasjonFindParams() {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            navn?.let { validate(StasjonFindDto::navn).isNotBlank() }
            id?.let { validate(StasjonFindDto::id).isGreaterThan(0) }
        }
    }
}