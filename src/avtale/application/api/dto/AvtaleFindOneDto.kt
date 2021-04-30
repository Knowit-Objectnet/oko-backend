package ombruk.backend.avtale.application.api.dto

import io.ktor.locations.*
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThan
import org.valiktor.validate

@KtorExperimentalLocationsAPI
@Location("/{id}") // TODO: remove?
data class AvtaleFindOneDto(
    val id: Int
) : IForm<AvtaleFindOneDto> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(AvtaleFindOneDto::id).isGreaterThan(0)
        }
    }
}