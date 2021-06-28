package ombruk.backend.aktor.application.api.dto

import kotlinx.serialization.Serializable
import ombruk.backend.aktor.application.service.IPartnerService
import ombruk.backend.aktor.application.service.IStasjonService
import ombruk.backend.aktor.domain.model.PartnerCreateParams
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
data class PartnerSaveDto(
    @Serializable( with = UUIDSerializer::class ) override val id: UUID? = null,
    override val navn: String,
    override val ideell: Boolean
) : IForm<PartnerSaveDto>, PartnerCreateParams(), KoinComponent {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(PartnerSaveDto::navn).isNotBlank() //.isUniqueInRepository(PartnerRepository)
            val partnerService: IPartnerService by inject()
            val stasjonService: IStasjonService by inject()
            validate(PartnerSaveDto::navn).isUniqueNavn(null, partnerService, stasjonService)
        }
    }
}