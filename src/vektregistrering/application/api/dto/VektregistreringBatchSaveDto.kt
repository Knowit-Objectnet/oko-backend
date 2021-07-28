package ombruk.backend.vektregistrering.application.api.dto

import arrow.core.Either
import kotlinx.serialization.Serializable
import ombruk.backend.henting.application.service.IHentingService
import ombruk.backend.kategori.domain.port.IKategoriRepository
import ombruk.backend.shared.error.ValidationError
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.valiktor.validate
import shared.model.serializer.UUIDSerializer
import java.util.*

@Serializable
data class VektregistreringBatchSaveDto(
    @Serializable(with = UUIDSerializer::class) val hentingId: UUID,
    val kategoriIds: List<String>,//Serializer library does not allow for serializing list of UUID
    val veiinger: List<Float>
) : IForm<VektregistreringBatchSaveDto>, KoinComponent {
    override fun validOrError(): Either<ValidationError, VektregistreringBatchSaveDto> = runCatchingValidation {
        validate(this) {
            val hentingService: IHentingService by inject()
            val kategoriRepository: IKategoriRepository by inject()

            val hentingExist: (UUID) -> Boolean = { transaction { hentingService.findOne(it) } is Either.Right }
            val myFun: (UUID) -> Boolean = { transaction { kategoriRepository.findOne(it) } is Either.Right }

            validate(VektregistreringBatchSaveDto::hentingId).isExistingUUID({ hentingExist(it) }, UUIDGenerelt)
            validate(VektregistreringBatchSaveDto::kategoriIds).allUUIDLegal(myFun)

            hentingService.findOne(hentingId).map { hentingWrapper ->
                hentingWrapper.ekstraHenting?.let { ekstrahenting ->
                    validate(VektregistreringBatchSaveDto::hentingId).isValidEkstrahenting(ekstrahenting)
                }

                hentingWrapper.planlagtHenting?.let { planlagtHenting ->
                    validate(VektregistreringBatchSaveDto::hentingId).isNotAvlyst(planlagtHenting)
                }
            }

            //TODO: Create a method to check every "veiinger" has a value above 0
        }
    }
}
