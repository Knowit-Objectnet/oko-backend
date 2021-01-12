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
@Location("/{id}")
data class PartnerDeleteForm(
    @get:Parameter(
        `in` = ParameterIn.PATH,
        name = "id",
        schema = Schema(type = "integer", format = "int32", nullable = false),
        description = "ID of Partner to delete",
        required = true
    ) val id: Int
) : IForm<PartnerDeleteForm> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(PartnerDeleteForm::id).isGreaterThan(0)
        }
    }

}