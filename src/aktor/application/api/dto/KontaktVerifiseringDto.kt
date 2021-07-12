package ombruk.backend.aktor.application.api.dto

import io.ktor.locations.*
import kotlinx.serialization.Serializable
import ombruk.backend.aktor.domain.model.KontaktFindParams
import ombruk.backend.aktor.domain.model.KontaktUpdateParams
import ombruk.backend.aktor.domain.model.KontaktVerifiserParams
import ombruk.backend.aktor.domain.model.VerifiseringUpdateParams
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isNotBlank
import org.valiktor.validate
import shared.model.serializer.UUIDSerializer
import java.util.*

@KtorExperimentalLocationsAPI
@Serializable
data class KontaktVerifiseringDto(
    @Serializable(with = UUIDSerializer::class) override val id: UUID,
    override val telefonKode: String? = null,
    override val epostKode: String? = null,
) : IForm<KontaktVerifiseringDto>, KontaktVerifiserParams() {
    override fun validOrError() = runCatchingValidation {
        validate(this) {}
    }
}