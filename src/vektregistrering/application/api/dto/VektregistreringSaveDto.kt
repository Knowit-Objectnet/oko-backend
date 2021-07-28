package ombruk.backend.vektregistrering.application.api.dto

import arrow.core.Either
import kotlinx.serialization.Serializable
import ombruk.backend.henting.application.service.IHentingService
import ombruk.backend.kategori.domain.port.IKategoriRepository
import ombruk.backend.shared.error.ValidationError
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.*
import ombruk.backend.vektregistrering.domain.params.VektregistreringCreateParams
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.valiktor.validate
import shared.model.serializer.UUIDSerializer
import java.util.*

@Serializable
data class VektregistreringSaveDto(
    @Serializable(with = UUIDSerializer::class) override val hentingId: UUID,
    @Serializable(with = UUIDSerializer::class) override val kategoriId: UUID,
    override val vekt: Float
) : IForm<VektregistreringSaveDto>, VektregistreringCreateParams(), KoinComponent {
    override fun validOrError(): Either<ValidationError, VektregistreringSaveDto> = runCatchingValidation {
        validate(this) {
            val hentingService: IHentingService by inject()
            val kategoriRepository: IKategoriRepository by inject()
            val hentingExist: (UUID) -> Boolean = { transaction { hentingService.findOne(it) } is Either.Right }
            val kategoriExist: (UUID) -> Boolean = { transaction { kategoriRepository.findOne(it) } is Either.Right }

            validate(VektregistreringSaveDto::hentingId).isExistingUUID({ hentingExist(it) }, UUIDGenerelt)
            validate(VektregistreringSaveDto::kategoriId).isExistingUUID({ kategoriExist(it) }, UUIDKategori)

            hentingService.findOne(hentingId).map { hentingWrapper ->
                hentingWrapper.ekstraHenting?.let { ekstrahenting ->
                    validate(VektregistreringSaveDto::hentingId).isValidEkstrahenting(ekstrahenting)
                }
            }

        }
    }
}