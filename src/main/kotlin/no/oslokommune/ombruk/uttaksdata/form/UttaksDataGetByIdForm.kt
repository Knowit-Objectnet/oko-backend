package no.oslokommune.ombruk.uttaksdata.form

import io.ktor.locations.Location
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Schema
import no.oslokommune.ombruk.shared.form.IForm
import no.oslokommune.ombruk.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThan
import org.valiktor.validate

@Location("/{id}")
data class UttaksDataGetByIdForm(
    @get:Parameter(
        `in` = ParameterIn.PATH,
        name = "id",
        schema = Schema(type = "integer", format = "int32", nullable = false),
        required = true,
        description = "The ID of the Uttaksdata to get"
    ) var id: Int
) : IForm<UttaksDataGetByIdForm> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(UttaksDataGetByIdForm::id).isGreaterThan(0)
        }
    }
}