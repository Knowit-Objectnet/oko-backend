package ombruk.backend.aktor.application.api.dto

import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThan
import org.valiktor.validate

@KtorExperimentalLocationsAPI
@Location("/{id}")
data class StasjonDeleteDto(val id: Int): IForm<StasjonDeleteDto> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(StasjonDeleteDto::id).isGreaterThan(0)
        }
    }
}