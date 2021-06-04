package ombruk.backend.henting.application.api.dto

import arrow.core.Either
import io.ktor.locations.*
import kotlinx.serialization.Serializable
import ombruk.backend.henting.domain.params.PlanlagtHentingFindParams
import ombruk.backend.henting.infrastructure.table.PlanlagtHentingTable
import ombruk.backend.shared.error.ValidationError
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.model.serializer.LocalDateTimeSerializer
import ombruk.backend.shared.utils.validation.isGreaterThanStartDateTime
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThanOrEqualTo
import org.valiktor.functions.isLessThanOrEqualTo
import org.valiktor.validate
import shared.model.serializer.UUIDSerializer
import java.time.LocalDateTime
import java.util.*

@KtorExperimentalLocationsAPI
@Serializable
@Location("")
data class PlanlagtHentingFindDto(
    @Serializable(with = UUIDSerializer::class) override val id: UUID? = null,
    @Serializable(with = LocalDateTimeSerializer::class) override val before: LocalDateTime? = null,
    @Serializable(with = LocalDateTimeSerializer::class) override val after: LocalDateTime? = null,
    override val merknad: String? = null,
    @Serializable(with = UUIDSerializer::class) override val henteplanId: UUID? = null,
    override val avlyst: Boolean? = null
) : IForm<PlanlagtHentingFindDto>, PlanlagtHentingFindParams() {
    override fun validOrError(): Either<ValidationError, PlanlagtHentingFindDto> = runCatchingValidation {
        validate(this) {
            if(before != null && after != null) {
                validate(PlanlagtHentingFindDto::before).isGreaterThanOrEqualTo(after)
            }
        }
    }
}