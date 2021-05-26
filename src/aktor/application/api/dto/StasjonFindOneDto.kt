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
    init {
        println("init 4 days")
    }
    override fun validOrError() = runCatchingValidation {
        println("VALIDATE THE UUID! $id")
        validate(this) {
            //UUID.fromString(id)
        }
    }
}