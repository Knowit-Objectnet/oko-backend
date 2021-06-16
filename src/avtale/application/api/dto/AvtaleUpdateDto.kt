package ombruk.backend.avtale.application.api.dto

import arrow.core.Either
import arrow.core.getOrElse
import henting.application.api.dto.HenteplanSaveDto
import kotlinx.serialization.Serializable
import ombruk.backend.aktor.application.service.IAktorService
import ombruk.backend.avtale.application.service.IAvtaleService
import ombruk.backend.avtale.domain.entity.Avtale
import ombruk.backend.avtale.domain.params.AvtaleCreateParams
import ombruk.backend.avtale.domain.params.AvtaleUpdateParams
import ombruk.backend.avtale.model.AvtaleType
import ombruk.backend.shared.error.ServiceError
import ombruk.backend.shared.error.ValidationError
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.model.serializer.LocalDateSerializer
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.valiktor.functions.isLessThan
import org.valiktor.functions.isLessThanOrEqualTo
import org.valiktor.functions.isValid
import org.valiktor.functions.validate
import org.valiktor.validate
import shared.model.serializer.UUIDSerializer
import java.time.LocalDate
import java.util.*

@Serializable
data class AvtaleUpdateDto(
    @Serializable(with = UUIDSerializer::class) override val id: UUID,
    override val type: AvtaleType? = null,
    @Serializable( with = LocalDateSerializer::class) override val startDato: LocalDate? = null,
    @Serializable( with = LocalDateSerializer::class) override val sluttDato: LocalDate? = null,
    ) : IForm<AvtaleUpdateDto>, AvtaleUpdateParams(), KoinComponent {
    override fun validOrError(): Either<ValidationError, AvtaleUpdateDto> = runCatchingValidation {
        validate(this) {
            //TODO: Lag en bedre sjekk, sjekk mot datoene til tidligere avtale om nødvendig.
            if (startDato != null && sluttDato != null) {
                validate(AvtaleUpdateDto::startDato).isLessThanOrEqualTo(sluttDato)
            }
        }
    }
}