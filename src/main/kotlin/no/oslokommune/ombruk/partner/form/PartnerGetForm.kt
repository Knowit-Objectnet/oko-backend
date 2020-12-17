package no.oslokommune.ombruk.partner.form

import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.Parameters
import io.swagger.v3.oas.annotations.enums.Explode
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.extensions.Extension
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import no.oslokommune.ombruk.shared.form.IForm
import no.oslokommune.ombruk.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isEmail
import org.valiktor.functions.isNotBlank
import org.valiktor.validate
import javax.ws.rs.FormParam
import javax.ws.rs.PathParam
import javax.ws.rs.*;
import javax.ws.rs.core.Context

@KtorExperimentalLocationsAPI
@Location("/")
data class PartnerGetForm(
    @get:Parameter(
        `in` = ParameterIn.QUERY,
        name = "navn",
        schema = Schema(type = "string", nullable = true)
    ) val navn: String? = null,
    @get:Parameter(
        `in` = ParameterIn.QUERY,
        name = "beskrivelse",
        schema = Schema(type = "string", nullable = true)
    ) val beskrivelse: String? = null,
    @get:Parameter(
        `in` = ParameterIn.QUERY,
        name = "telefon",
        schema = Schema(type = "string", nullable = true)
    ) val telefon: String? = null,
    @get:Parameter(
        `in` = ParameterIn.QUERY,
        name = "epost",
        schema = Schema(type = "string", nullable = true)
    ) val epost: String? = null
) : IForm<PartnerGetForm> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            if (navn != null) validate(PartnerGetForm::navn).isNotBlank()
        }

    }
}