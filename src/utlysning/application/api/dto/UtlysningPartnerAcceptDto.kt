package ombruk.backend.utlysning.application.api.dto

import io.ktor.locations.*
import kotlinx.serialization.Serializable
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.runCatchingValidation
import ombruk.backend.utlysning.domain.params.UtlysningPartnerAcceptParams
import org.valiktor.validate
import shared.model.serializer.UUIDSerializer
import java.util.*

@Serializable
@KtorExperimentalLocationsAPI
@Location("/partner-aksepter")
data class UtlysningPartnerAcceptDto(
    @Serializable(with = UUIDSerializer::class) override val id: UUID,
    override val toAccept: Boolean
) : IForm<UtlysningPartnerAcceptDto>, UtlysningPartnerAcceptParams() {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
        }
    }
}