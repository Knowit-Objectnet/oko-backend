package ombruk.backend.avtale.application.api.dto

import io.ktor.locations.*
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThan
import org.valiktor.validate
import java.util.*

@KtorExperimentalLocationsAPI
@Location("/{id}") // TODO: remove?
data class AvtaleFindOneDto(
    val id: UUID
) : IForm<AvtaleFindOneDto> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
        }
    }
}