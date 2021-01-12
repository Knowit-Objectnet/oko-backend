package no.oslokommune.ombruk.uttak.form

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
data class UttakGetByIdForm(
    @get:Parameter(
        `in` = ParameterIn.PATH,
        name = "id",
        schema = Schema(type = "integer", format = "int32", nullable = false),
        required = true,
        description = "Get the Uttak with this specific ID"
    ) val id: Int
) : IForm<UttakGetByIdForm> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(UttakGetByIdForm::id).isGreaterThan(0)
        }
    }
}