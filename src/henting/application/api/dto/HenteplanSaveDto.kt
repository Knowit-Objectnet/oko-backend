package henting.application.api.dto

import arrow.core.Either
import arrow.core.extensions.either.applicative.applicative
import arrow.core.extensions.list.traverse.sequence
import arrow.core.fix
import arrow.core.left
import arrow.core.right
import kotlinx.serialization.Serializable
import ombruk.backend.avtale.domain.entity.Avtale
import ombruk.backend.avtale.domain.port.IAvtaleRepository
import ombruk.backend.avtale.infrastructure.repository.AvtaleRepository
import ombruk.backend.henting.domain.model.HenteplanFrekvens
import ombruk.backend.henting.domain.params.HenteplanCreateParams
import ombruk.backend.kategori.application.api.dto.HenteplanKategoriBatchSaveDto
import ombruk.backend.kategori.application.api.dto.HenteplanKategoriSaveDto
import ombruk.backend.kategori.application.api.dto.IKategoriKoblingSaveDto
import ombruk.backend.kategori.domain.entity.Kategori
import ombruk.backend.kategori.domain.port.IKategoriRepository
import ombruk.backend.shared.error.ValidationError
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.model.serializer.LocalDateTimeSerializer
import ombruk.backend.shared.utils.validation.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.valiktor.functions.isGreaterThanOrEqualTo
import org.valiktor.functions.isLessThanOrEqualTo
import org.valiktor.functions.isNotNull
import org.valiktor.functions.isValid
import org.valiktor.validate
import shared.model.serializer.UUIDSerializer
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

@Serializable
data class HenteplanSaveDto(
    @Serializable(with = UUIDSerializer::class) override val avtaleId: UUID,
    @Serializable(with = UUIDSerializer::class) override val stasjonId: UUID,
    override val frekvens: HenteplanFrekvens,
    @Serializable(with = LocalDateTimeSerializer::class) override val startTidspunkt: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class) override val sluttTidspunkt: LocalDateTime,
    override var ukedag: DayOfWeek? = null,
    override val merknad: String? = null,
    var kategorier: List<HenteplanKategoriBatchSaveDto>? = null
) : IForm<HenteplanSaveDto>, HenteplanCreateParams(), KoinComponent {
    override fun validOrError(): Either<ValidationError, HenteplanSaveDto> = runCatchingValidation {
        validate(this) {
            validate(HenteplanSaveDto::sluttTidspunkt).isGreaterThanStartDateTime(startTidspunkt)
            if (frekvens != HenteplanFrekvens.ENKELT) {
                validate(HenteplanSaveDto::ukedag).isNotNull()
            }
            if(frekvens == HenteplanFrekvens.ENKELT) {
                ukedag = startTidspunkt.dayOfWeek
            }

            val avtaleRepository: IAvtaleRepository = get()

            val avtaleEither = transaction { avtaleRepository.findOne(avtaleId) }

            avtaleEither.bimap(
                {validate(HenteplanSaveDto::avtaleId).isValid { false }}, //TODO: Use custom validator to give good response
                {
                    validate(HenteplanSaveDto::startTidspunkt).isGreaterThanOrEqualTo(LocalDateTime.of(it.startDato, LocalTime.MIN))
                    validate(HenteplanSaveDto::sluttTidspunkt).isLessThanOrEqualTo(LocalDateTime.of(it.sluttDato, LocalTime.MAX))
                }
            )

            if (kategorier != null) {
                val kategoriRepository: IKategoriRepository by inject()
                val exist: (UUID) -> Boolean = { transaction { kategoriRepository.findOne(it) } is Either.Right }
                validate(HenteplanSaveDto::kategorier).isExistingUUID({ it.all { exist(it.kategoriId) } }, UUIDKategori)
            }
        }
    }
}