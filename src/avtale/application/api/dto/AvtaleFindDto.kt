package ombruk.backend.avtale.application.api.dto

import arrow.core.Either
import io.ktor.locations.*
import kotlinx.serialization.Serializable
import ombruk.backend.avtale.domain.params.AvtaleFindParams
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

@Location("")
@Serializable
data class AvtaleFindDto(
    @Serializable(with = UUIDSerializer::class) override val aktorId: UUID? = null,
    override val type: AvtaleType? = null,
    @Serializable(with = UUIDSerializer::class) override val id: UUID? = null,
    @Serializable( with = LocalDateSerializer::class) override val startDato: LocalDate? = null,
    @Serializable( with = LocalDateSerializer::class) override val sluttDato: LocalDate? = null,
) : IForm<AvtaleFindDto>, AvtaleFindParams() {
    override fun validOrError(): Either<ValidationError, AvtaleFindDto> = runCatchingValidation {
        validate(this) {

            if(startDato != null && sluttDato != null) {
                validate(AvtaleFindDto::startDato).isLessThan(sluttDato)
            }

        }
    }
}