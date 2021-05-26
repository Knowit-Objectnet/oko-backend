package ombruk.backend.aktor.application.api.dto

import arrow.core.right
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.validate
import java.util.*

@KtorExperimentalLocationsAPI
@Location("/{id}")
data class StasjonFindOneDto(val id: String) : IForm<StasjonFindOneDto> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            // @TODO fix uuid check
        }
    }
}