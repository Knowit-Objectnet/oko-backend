package ombruk.backend.utlysning.application.api.dto

import io.ktor.locations.*
import kotlinx.serialization.Serializable
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.runCatchingValidation
import ombruk.backend.utlysning.domain.params.UtlysningFindParams
import org.valiktor.functions.isNotBlank
import org.valiktor.validate
import shared.model.serializer.UUIDSerializer
import java.time.LocalDateTime
import java.util.*

@KtorExperimentalLocationsAPI
@Location("")
@Serializable
data class UtlysningFindDto(
    @Serializable(with = UUIDSerializer::class) override val id: UUID? = null,
    @Serializable(with = UUIDSerializer::class) override val partnerId: UUID? = null,
    @Serializable(with = UUIDSerializer::class) override val hentingId: UUID? = null,
    override val partnerPameldt: Boolean? = null,
    override val stasjonGodkjent: Boolean? = null,
    override val partnerSkjult: Boolean? = null,
    override val partnerVist: Boolean? = null,
) : IForm<UtlysningFindDto>,
    UtlysningFindParams() {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
        }
    }
}