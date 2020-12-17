package no.oslokommune.ombruk.stasjon.form

import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Schema
import no.oslokommune.ombruk.shared.form.IForm
import no.oslokommune.ombruk.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isNotBlank
import org.valiktor.validate
import javax.ws.rs.BeanParam
import javax.ws.rs.QueryParam

@KtorExperimentalLocationsAPI
@Location("/")
data class StasjonGetForm(
    @get:Parameter(`in` = ParameterIn.QUERY, name = "navn") val navn: String? = null
) : IForm<StasjonGetForm> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(StasjonGetForm::navn).isNotBlank()
        }
    }
}