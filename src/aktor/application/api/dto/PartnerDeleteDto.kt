package ombruk.backend.aktor.application.api.dto

import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThan
import org.valiktor.validate

@KtorExperimentalLocationsAPI
@Location("/{id}")
data class PartnerDeleteDto(
val id: Int
) : IForm<PartnerDeleteDto> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(PartnerDeleteDto::id).isGreaterThan(0)
        }
    }

}