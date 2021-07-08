package ombruk.backend.aarsak.application.api.dto

import kotlinx.serialization.Serializable
import ombruk.backend.aarsak.domain.model.AarsakUpdateParams
import ombruk.backend.aktor.application.service.IPartnerService
import ombruk.backend.aktor.application.service.IStasjonService
import ombruk.backend.aktor.domain.enum.StasjonType
import ombruk.backend.aktor.domain.model.StasjonUpdateParams
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.isUniqueNavn
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.valiktor.functions.isNotBlank
import org.valiktor.validate
import shared.model.serializer.UUIDSerializer
import java.util.*

@Serializable
data class AarsakUpdateDto(
    @Serializable(with = UUIDSerializer::class) override val id: UUID,
    override val beskrivelse: String? = null,
) : IForm<AarsakUpdateDto>, AarsakUpdateParams() {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            beskrivelse?.let{validate(AarsakUpdateDto::beskrivelse).isNotBlank()}
        }
    }
}