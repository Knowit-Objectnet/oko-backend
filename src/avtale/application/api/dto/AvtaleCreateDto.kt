package avtale.application.api.dto

import arrow.core.Either
import henting.application.api.dto.HenteplanPostDto
import kotlinx.serialization.Serializable
import ombruk.backend.avtale.domain.params.AvtaleCreateParams
import ombruk.backend.avtale.model.AvtaleType
import ombruk.backend.shared.error.ValidationError
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.model.serializer.LocalDateSerializer
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isPositive
import org.valiktor.validate
import shared.model.serializer.UUIDSerializer
import java.time.LocalDate
import java.util.*

@Serializable
data class AvtalePostDto(
    @Serializable( with = UUIDSerializer::class)
    override val aktorId: String,
    override val type: AvtaleType,
    override val henteplaner: List<HenteplanPostDto>,
    @Serializable( with = LocalDateSerializer::class)
    override val startDato: LocalDate,
    @Serializable( with = LocalDateSerializer::class)
    override val sluttDato: LocalDate
) : IForm<AvtalePostDto>, AvtaleCreateParams() {
    override fun validOrError(): Either<ValidationError, AvtalePostDto> = runCatchingValidation {
        validate(this) {
            validate(AvtalePostDto::aktorId)
        }
    }
}