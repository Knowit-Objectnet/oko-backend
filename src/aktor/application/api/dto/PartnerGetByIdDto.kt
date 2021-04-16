package ombruk.backend.aktor.application.api.dto

import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThan
import org.valiktor.validate
@KtorExperimentalLocationsAPI
@Location("/{id}") // TODO: remove?
data class PartnerGetByIdDto(
val id: Int
) : IForm<PartnerGetByIdDto> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(PartnerGetByIdDto::id).isGreaterThan(0)
        }
    }
}