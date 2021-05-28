package avtale.application.api.dto

import arrow.core.Either
import henting.application.api.dto.HenteplanInsertDto
import kotlinx.serialization.Serializable
import ombruk.backend.avtale.domain.params.AvtaleCreateParams
import ombruk.backend.avtale.model.AvtaleType
import ombruk.backend.shared.error.ValidationError
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.model.serializer.LocalDateSerializer
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isLessThan
import org.valiktor.validate
import shared.model.serializer.UUIDSerializer
import java.time.LocalDate
import java.util.*

@Serializable
data class AvtaleInsertDto(
    @Serializable(with = UUIDSerializer::class) override val aktorId: UUID,
    override val type: AvtaleType,
    @Serializable( with = LocalDateSerializer::class) override val startDato: LocalDate,
    @Serializable( with = LocalDateSerializer::class) override val sluttDato: LocalDate,
    override val henteplaner: List<HenteplanInsertDto>?
    ) : IForm<AvtaleInsertDto>, AvtaleCreateParams() {
    override fun validOrError(): Either<ValidationError, AvtaleInsertDto> = runCatchingValidation {
        validate(this) {
            validate(AvtaleInsertDto::startDato).isLessThan(sluttDato)
        }
    }
}