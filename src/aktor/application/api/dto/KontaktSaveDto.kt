package ombruk.backend.aktor.application.api.dto

import arrow.core.Either
import kotlinx.serialization.Serializable
import ombruk.backend.aktor.application.service.IAktorService
import ombruk.backend.aktor.domain.model.KontaktCreateParams
import ombruk.backend.aktor.domain.model.PartnerCreateParams
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.isNorwegianPhoneNumber
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.valiktor.functions.isEmail
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
            if (telefon != null && telefon.trim().isNotEmpty()) validate(KontaktSaveDto::telefon).isNorwegianPhoneNumber()
            if (epost != null && epost.trim().isNotEmpty()) validate(KontaktSaveDto::epost).isEmail()
        }.copy(telefon = telefon?.trim(), epost = epost?.trim())
    }

}