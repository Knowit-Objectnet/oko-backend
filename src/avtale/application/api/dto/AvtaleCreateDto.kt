package avtale.application.api.dto

import arrow.core.Either
import henting.application.api.dto.HenteplanPostDto
import kotlinx.serialization.Serializable
import ombruk.backend.avtale.domain.params.AvtaleCreateParams
import ombruk.backend.avtale.model.AvtaleType
import ombruk.backend.shared.error.ValidationError
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isPositive
import org.valiktor.validate

@Serializable
data class AvtalePostDto(
    override val aktorId: Int,
    override val type: AvtaleType,
    override val henteplaner: List<HenteplanPostDto>
) : IForm<AvtalePostDto>, AvtaleCreateParams() {
    override fun validOrError(): Either<ValidationError, AvtalePostDto> = runCatchingValidation {
        validate(this) {
            validate(AvtalePostDto::aktorId).isPositive()
        }
    }
}