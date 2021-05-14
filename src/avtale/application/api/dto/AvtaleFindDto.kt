package ombruk.backend.avtale.application.api.dto

import arrow.core.Either
import arrow.core.invalid
import avtale.application.api.dto.AvtaleCreateDto
import io.ktor.locations.*
import kotlinx.serialization.Serializable
import ombruk.backend.avtale.domain.params.AvtaleFindParams
import ombruk.backend.avtale.model.AvtaleType
import ombruk.backend.shared.error.ValidationError
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.model.serializer.LocalDateSerializer
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.ConstraintViolationException
import org.valiktor.functions.isLessThan
import org.valiktor.functions.isPositive
import org.valiktor.functions.isValid
import org.valiktor.validate
import shared.model.serializer.UUIDSerializer
import java.lang.Exception
import java.time.LocalDate
import java.util.*

@Location("/")
@Serializable(with = UUIDSerializer::class)
data class AvtaleFindDto(
    override val aktorId: UUID? = null,
    override val type: AvtaleType? = null,
    override val id: UUID? = null,
    @Serializable( with = LocalDateSerializer::class) override val startDato: LocalDate? = null,
    @Serializable( with = LocalDateSerializer::class) override val sluttDato: LocalDate? = null
) : IForm<AvtaleFindDto>, AvtaleFindParams() {
    override fun validOrError(): Either<ValidationError, AvtaleFindDto> = runCatchingValidation {
        validate(this) {

            if(startDato != null && sluttDato != null) {
                validate(AvtaleFindDto::startDato).isLessThan(sluttDato)
            }

        }
    }
}