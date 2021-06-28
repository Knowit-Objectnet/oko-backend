package ombruk.backend.aktor.application.api.dto

import kotlinx.serialization.Serializable
import ombruk.backend.aktor.application.service.IPartnerService
import ombruk.backend.aktor.application.service.IStasjonService
import ombruk.backend.aktor.domain.enum.StasjonType
import ombruk.backend.aktor.domain.model.StasjonCreateParams
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.isUniqueNavn
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.valiktor.functions.isNotBlank
import org.valiktor.validate

@Serializable
data class StasjonSaveDto(
    override val navn: String,
    override val type: StasjonType
) : IForm<StasjonSaveDto>, StasjonCreateParams(), KoinComponent {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(StasjonSaveDto::navn).isNotBlank()
            val partnerService: IPartnerService by inject()
            val stasjonService: IStasjonService by inject()
            validate(StasjonSaveDto::navn).isUniqueNavn(partnerService, stasjonService)
            //FIXME: Validate UUID?
        }
    }
}