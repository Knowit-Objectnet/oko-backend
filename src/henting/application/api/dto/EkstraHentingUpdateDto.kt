package ombruk.backend.henting.application.api.dto

import arrow.core.Either
import kotlinx.serialization.Serializable
import ombruk.backend.henting.domain.params.EkstraHentingUpdateParams
import ombruk.backend.henting.domain.port.IEkstraHentingRepository
import ombruk.backend.henting.domain.port.IHenteplanRepository
import ombruk.backend.kategori.application.api.dto.EkstraHentingKategoriBatchSaveDto
import ombruk.backend.kategori.domain.port.IKategoriRepository
import ombruk.backend.shared.error.ValidationError
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.model.serializer.LocalDateTimeSerializer
import ombruk.backend.shared.utils.validation.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.valiktor.validate
import shared.model.serializer.UUIDSerializer
import java.time.LocalDateTime
import java.util.*

@Serializable
data class EkstraHentingUpdateDto(
    @Serializable(with = UUIDSerializer::class) override val id: UUID,
    @Serializable(with = LocalDateTimeSerializer::class) override val startTidspunkt: LocalDateTime? = null,
    @Serializable(with = LocalDateTimeSerializer::class) override val sluttTidspunkt: LocalDateTime? = null,
    override val merknad: String? = null,
    var kategorier: List<EkstraHentingKategoriBatchSaveDto>? = null
) : IForm<EkstraHentingUpdateDto>, EkstraHentingUpdateParams(), KoinComponent {
    override fun validOrError(): Either<ValidationError, EkstraHentingUpdateDto> = runCatchingValidation {
        validate(this) {
            if (startTidspunkt != null && sluttTidspunkt != null) {
                validate(EkstraHentingUpdateDto::sluttTidspunkt).isGreaterThanStartDateTime(startTidspunkt)
            } else if (startTidspunkt != null || sluttTidspunkt != null) {
                transaction { get<IEkstraHentingRepository>().findOne(it.id) }.map {
                    if (startTidspunkt != null) validate(EkstraHentingUpdateDto::startTidspunkt).isLessThanEndDateTime(
                        it.sluttTidspunkt
                    )
                    if (sluttTidspunkt != null) validate(EkstraHentingUpdateDto::sluttTidspunkt).isGreaterThanStartDateTime(
                        it.startTidspunkt
                    )
                }
            }

            if (kategorier != null) {
                val kategoriRepository: IKategoriRepository by inject()
                val exist: (UUID) -> Boolean = { transaction { kategoriRepository.findOne(it) } is Either.Right }
                validate(EkstraHentingUpdateDto::kategorier).isExistingUUID({ it.all { exist(it.kategoriId) } }, UUIDKategori)
            }
        }
    }
}