package ombruk.backend.aktor.application.api.dto

import arrow.core.Either
import kotlinx.serialization.Serializable
import ombruk.backend.aktor.application.service.IAktorService
import ombruk.backend.aktor.domain.model.KontaktCreateParams
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
data class KontaktSaveDto(
    @Serializable(with = UUIDSerializer::class) override val aktorId: UUID,
    override val navn: String,
    override val telefon: String? = null,
    override val epost: String? = null,
    override val rolle: String? = null,
) : IForm<KontaktSaveDto>, KontaktCreateParams(), KoinComponent {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            val aktorService: IAktorService by inject()
            validate(KontaktSaveDto::aktorId).isValid {
                aktorService.findOne(it) is Either.Right
            }
            validate(KontaktSaveDto::navn).isNotBlank()
            validate(KontaktSaveDto::telefon).isNorwegianPhoneNumberOrBlank()
            validate(KontaktSaveDto::epost).isEmailOrBlank()
        }.copy(telefon = telefon?.trim(), epost = epost?.trim(), rolle = rolle?.trim())
    }

}