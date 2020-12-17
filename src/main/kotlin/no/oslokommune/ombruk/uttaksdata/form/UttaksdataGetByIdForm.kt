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
data class UttaksdataGetByIdForm(
    @get:Parameter(
        `in` = ParameterIn.PATH,
        name = "id",
        schema = Schema(type = "int32"),
        required = true,
        description = "The ID of the Uttaksdata to get"
    ) var id: Int
) : IForm<UttaksdataGetByIdForm> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(UttaksdataGetByIdForm::id).isGreaterThan(0)

        }
    }
}