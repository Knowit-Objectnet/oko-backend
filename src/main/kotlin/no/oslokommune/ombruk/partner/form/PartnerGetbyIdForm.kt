package no.oslokommune.ombruk.partner.form

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
@Location("/{id}") // TODO: remove?
data class PartnerGetByIdForm(
    @get:Parameter(
        `in` = ParameterIn.PATH,
        name = "id",
        schema = Schema(type = "integer", nullable = false, format = "int32"),
        description = "ID of Partner to get",
        required = true
    ) val id: Int
) : IForm<PartnerGetByIdForm> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(PartnerGetByIdForm::id).isGreaterThan(0)
        }
    }
}