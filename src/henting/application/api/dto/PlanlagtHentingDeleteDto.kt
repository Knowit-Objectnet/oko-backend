package ombruk.backend.henting.application.api.dto

import arrow.core.Either
import io.ktor.locations.*
import kotlinx.serialization.Serializable
import ombruk.backend.shared.error.ValidationError
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.validate
import shared.model.serializer.UUIDSerializer
import java.util.*

@KtorExperimentalLocationsAPI
@Serializable(with = UUIDSerializer::class)
@Location("/{id}")
data class PlanlagtHentingDeleteDto(val id: UUID): IForm<PlanlagtHentingDeleteDto> {
    override fun validOrError(): Either<ValidationError, PlanlagtHentingDeleteDto> = runCatchingValidation{
        validate(this) {
        }
    }
}