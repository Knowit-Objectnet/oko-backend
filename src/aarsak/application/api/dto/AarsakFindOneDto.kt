package ombruk.backend.aarsak.application.api.dto

import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.validate
import java.util.*

@KtorExperimentalLocationsAPI
@Location("/{id}")
data class AarsakFindOneDto(val id: UUID) : IForm<AarsakFindOneDto> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            //FIXME: Validate UUID?
        }
    }
}