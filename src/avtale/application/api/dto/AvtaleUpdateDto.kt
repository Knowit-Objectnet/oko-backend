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
import ombruk.backend.avtale.domain.port.IAvtaleRepository
import ombruk.backend.avtale.model.AvtaleType
import ombruk.backend.henting.application.api.dto.HenteplanUpdateDto
import ombruk.backend.henting.domain.port.IHenteplanRepository
import ombruk.backend.shared.error.ServiceError
import ombruk.backend.shared.error.ValidationError
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.model.serializer.LocalDateSerializer
import ombruk.backend.shared.utils.validation.isGreaterThanStartDateTime
import ombruk.backend.shared.utils.validation.isLessThanEndDateTime
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.valiktor.functions.*
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
    override val saksnummer: String? = null,
    ) : IForm<AvtaleUpdateDto>, AvtaleUpdateParams(), KoinComponent {
    override fun validOrError(): Either<ValidationError, AvtaleUpdateDto> = runCatchingValidation {
        validate(this) {
            if (startDato != null && sluttDato != null) {
                validate(AvtaleUpdateDto::sluttDato).isGreaterThanOrEqualTo(startDato)
            } else if (startDato != null || sluttDato != null) {
                transaction { get<IAvtaleRepository>().findOne(it.id) }.map {
                    if (startDato != null) validate(AvtaleUpdateDto::startDato).isLessThanOrEqualTo(it.sluttDato)
                    if (sluttDato != null) validate(AvtaleUpdateDto::sluttDato).isGreaterThanOrEqualTo(it.startDato)
                }
            }
        }
    }
}