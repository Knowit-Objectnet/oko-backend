package ombruk.backend.henting.application.api.dto

import arrow.core.Either
import kotlinx.serialization.Serializable
import ombruk.backend.henting.domain.params.EkstraHentingCreateParams
import ombruk.backend.kategori.application.api.dto.EkstraHentingKategoriBatchSaveDto
import ombruk.backend.kategori.domain.port.IKategoriRepository
import ombruk.backend.shared.error.ValidationError
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.model.serializer.LocalDateTimeSerializer
import ombruk.backend.shared.utils.validation.allValidUUIDEkstraHenting
import ombruk.backend.shared.utils.validation.isGreaterThanStartDateTime
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.valiktor.validate
import shared.model.serializer.UUIDSerializer
import java.time.LocalDateTime
import java.util.*

@Serializable
data class EkstraHentingSaveDto(
    @Serializable(with = LocalDateTimeSerializer::class) override val startTidspunkt: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class) override val sluttTidspunkt: LocalDateTime,
    override val merknad: String? = null,
    @Serializable(with = UUIDSerializer::class) override val stasjonId: UUID,
    var kategorier: List<EkstraHentingKategoriBatchSaveDto>? = null
) : IForm<EkstraHentingSaveDto>, EkstraHentingCreateParams(), KoinComponent {
    override fun validOrError(): Either<ValidationError, EkstraHentingSaveDto> = runCatchingValidation {
        validate(this) {
            validate(EkstraHentingSaveDto::sluttTidspunkt).isGreaterThanStartDateTime(startTidspunkt)

            if (kategorier != null) {
                val kategoriRepository: IKategoriRepository by inject()
                val exist: (UUID) -> Boolean = { transaction { kategoriRepository.findOne(it) } is Either.Right }
                validate(EkstraHentingSaveDto::kategorier).allValidUUIDEkstraHenting(exist)
            }
        }
    }
}