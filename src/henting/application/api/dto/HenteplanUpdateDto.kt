package ombruk.backend.henting.application.api.dto

import arrow.core.Either
import henting.application.api.dto.HenteplanSaveDto
import kotlinx.serialization.Serializable
import ombruk.backend.avtale.domain.port.IAvtaleRepository
import ombruk.backend.henting.domain.model.HenteplanFrekvens
import ombruk.backend.henting.domain.params.HenteplanUpdateParams
import ombruk.backend.henting.domain.port.IHenteplanRepository
import ombruk.backend.kategori.application.api.dto.HenteplanKategoriBatchSaveDto
import ombruk.backend.kategori.domain.port.IKategoriRepository
import ombruk.backend.shared.error.ValidationError
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.model.serializer.LocalDateTimeSerializer
import ombruk.backend.shared.utils.validation.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.valiktor.functions.*
import org.valiktor.validate
import shared.model.serializer.UUIDSerializer
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

@Serializable
data class HenteplanUpdateDto(
    @Serializable(with = UUIDSerializer::class) override val id: UUID,
    override val frekvens: HenteplanFrekvens? = null,
    @Serializable(with = LocalDateTimeSerializer::class) override val startTidspunkt: LocalDateTime? = null,
    @Serializable(with = LocalDateTimeSerializer::class) override val sluttTidspunkt: LocalDateTime? = null,
    override val ukeDag: DayOfWeek? = null,
    override val merknad: String? = null,
    var kategorier: List<HenteplanKategoriBatchSaveDto>? = null
) : IForm<HenteplanUpdateDto>, HenteplanUpdateParams(), KoinComponent {
    override fun validOrError(): Either<ValidationError, HenteplanUpdateDto> = runCatchingValidation {
        validate(this) {
            if (startTidspunkt != null && sluttTidspunkt != null) {
                validate(HenteplanUpdateDto::sluttTidspunkt).isGreaterThanStartDateTime(startTidspunkt)
            } else if (startTidspunkt != null || sluttTidspunkt != null) {
                transaction { get<IHenteplanRepository>().findOne(it.id) }.map {
                    if (startTidspunkt != null) validate(HenteplanUpdateDto::startTidspunkt).isLessThanEndDateTime(it.sluttTidspunkt)
                    if (sluttTidspunkt != null) validate(HenteplanUpdateDto::sluttTidspunkt).isGreaterThanStartDateTime(it.startTidspunkt)
                }
            }

            if (frekvens != null) {
                if (frekvens != HenteplanFrekvens.ENKELT) {
                    validate(HenteplanUpdateDto::ukeDag).isNotNull()
                }
            }

            val henteplanRepository: IHenteplanRepository = get()
            val avtaleRepository: IAvtaleRepository = get()

            // Date check if valid
            transaction { henteplanRepository.findOne(it.id) }.map {
               transaction {
                   avtaleRepository.findOne(it.avtaleId).map {
                       validate(HenteplanUpdateDto::startTidspunkt).isGreaterThanOrEqualTo(LocalDateTime.of(it.startDato, LocalTime.MIN))
                       validate(HenteplanUpdateDto::sluttTidspunkt).isLessThanOrEqualTo(LocalDateTime.of(it.sluttDato, LocalTime.MAX))
                   }
               }
            }
            if (kategorier != null) {
                val kategoriRepository: IKategoriRepository by inject()
                val exist: (UUID) -> Boolean = { transaction { kategoriRepository.findOne(it) } is Either.Right }
                validate(HenteplanUpdateDto::kategorier).isValidKategori { it.all { exist(it.kategoriId) } }
            }
        }
    }
}