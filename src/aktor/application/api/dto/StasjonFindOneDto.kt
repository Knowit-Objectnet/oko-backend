package ombruk.backend.aktor.application.api.dto

import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThan
import org.valiktor.validate
import java.util.*

@KtorExperimentalLocationsAPI
@Location("/{id}")
data class StasjonFindOneDto(val id: UUID) : IForm<StasjonFindOneDto> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
//            validate(StasjonFindOneDto::id).isGreaterThan(0)
            //FIXME: Validate UUID?
        }
    }
}