package ombruk.backend.aktor.application.api.dto

import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import kotlinx.serialization.Serializable
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThan
import org.valiktor.validate
import shared.model.serializer.UUIDSerializer
import java.util.*

@KtorExperimentalLocationsAPI
@Location("/{id}")
data class KontaktDeleteDto(
    @Serializable(with = UUIDSerializer::class) val id: UUID) : IForm<KontaktDeleteDto> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
        }
    }

}