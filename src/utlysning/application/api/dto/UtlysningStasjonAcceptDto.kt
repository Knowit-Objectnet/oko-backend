package ombruk.backend.utlysning.application.api.dto

import io.ktor.locations.*
import kotlinx.serialization.Serializable
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.runCatchingValidation
import ombruk.backend.utlysning.domain.params.UtlysningStasjonAcceptParams
import org.valiktor.validate
import shared.model.serializer.UUIDSerializer
import java.util.*

@Serializable
@KtorExperimentalLocationsAPI
@Location("/stasjon-aksepter")
data class UtlysningStasjonAcceptDto(
    @Serializable(with = UUIDSerializer::class) override val id: UUID,
    override val toAccept: Boolean
) : IForm<UtlysningStasjonAcceptDto>, UtlysningStasjonAcceptParams() {
    override fun validOrError() = runCatchingValidation {
        //TODO: Should maybe validate if partner has already accepted
        validate(this) {
        }
    }
}