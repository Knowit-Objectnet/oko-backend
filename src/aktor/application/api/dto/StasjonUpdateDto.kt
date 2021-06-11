package ombruk.backend.aktor.application.api.dto

import kotlinx.serialization.Serializable
import ombruk.backend.aktor.domain.enum.StasjonType
import ombruk.backend.aktor.domain.model.StasjonUpdateParams
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThan
import org.valiktor.functions.isNotBlank
import org.valiktor.validate
import shared.model.serializer.UUIDSerializer
import java.util.*

@Serializable
data class StasjonUpdateDto(
    @Serializable(with = UUIDSerializer::class) override val id: UUID,
    override val navn: String? = null,
    override val type: StasjonType? = null
) : IForm<StasjonUpdateDto>, StasjonUpdateParams() {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            navn?.let{validate(StasjonUpdateDto::navn).isNotBlank()}
        }
    }
}