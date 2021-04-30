package ombruk.backend.avtale.application.api.dto

import arrow.core.Either
import io.ktor.locations.*
import kotlinx.serialization.Serializable
import ombruk.backend.avtale.domain.params.AvtaleFindParams
import ombruk.backend.avtale.model.AvtaleType
import ombruk.backend.shared.error.ValidationError
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isPositive
import org.valiktor.validate

@Location("/")
@Serializable
data class AvtaleFindDto(
    override val aktorId: Int?,
    override val type: AvtaleType?,
    override val id: Int?
) : IForm<AvtaleFindDto>, AvtaleFindParams() {
    override fun validOrError(): Either<ValidationError, AvtaleFindDto> = runCatchingValidation {
        validate(this) {
            aktorId?.let { validate(AvtaleFindDto::aktorId).isPositive() }
            id?.let { validate(AvtaleFindDto::id).isPositive() }
        }
    }
}