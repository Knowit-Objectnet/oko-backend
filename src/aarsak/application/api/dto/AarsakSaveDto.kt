package ombruk.backend.aarsak.application.api.dto

import kotlinx.serialization.Serializable
import ombruk.backend.aarsak.domain.model.AarsakCreateParams
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isNotBlank
import org.valiktor.validate
import shared.model.serializer.UUIDSerializer
import java.util.*

@Serializable
data class AarsakSaveDto(
    @Serializable( with = UUIDSerializer::class ) override val id: UUID?,
    override val beskrivelse: String,
) : IForm<AarsakSaveDto>, AarsakCreateParams() {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(AarsakSaveDto::beskrivelse).isNotBlank()
        }
    }
}