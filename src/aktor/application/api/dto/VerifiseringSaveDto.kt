package ombruk.backend.aktor.application.api.dto

import arrow.core.Either
import kotlinx.serialization.Serializable
import ombruk.backend.aktor.application.service.IAktorService
import ombruk.backend.aktor.domain.model.KontaktCreateParams
import ombruk.backend.aktor.domain.model.VerifiseringCreateParams
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.isNorwegianPhoneNumberOrBlank
import ombruk.backend.shared.utils.validation.isEmailOrBlank
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.valiktor.functions.isNotBlank
import org.valiktor.functions.isValid
import org.valiktor.validate
import shared.model.serializer.UUIDSerializer
import java.util.*

@Serializable
data class VerifiseringSaveDto(
    @Serializable(with = UUIDSerializer::class) override val id: UUID,
    override val telefonKode: String? = null,
    override val telefonVerifisert: Boolean? = null,
    override val epostKode: String? = null,
    override val epostVerifisert: Boolean? = null
) : IForm<VerifiseringSaveDto>, VerifiseringCreateParams(), KoinComponent {
    override fun validOrError() = runCatchingValidation {
        validate(this) {}
    }

}