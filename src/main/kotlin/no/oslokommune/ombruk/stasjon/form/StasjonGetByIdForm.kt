package no.oslokommune.ombruk.stasjon.form

import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Schema
import no.oslokommune.ombruk.shared.form.IForm
import no.oslokommune.ombruk.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThan
import org.valiktor.validate

@KtorExperimentalLocationsAPI
@Location("/{id}")
data class StasjonGetByIdForm(
    @get:Parameter(
        `in` = ParameterIn.PATH,
        name = "id",
        schema = Schema(type = "int32"),
        required = true,
        description = "ID of Stasjon to return"
    ) val id: Int
) : IForm<StasjonGetByIdForm> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(StasjonGetByIdForm::id).isGreaterThan(0)
        }
    }
}