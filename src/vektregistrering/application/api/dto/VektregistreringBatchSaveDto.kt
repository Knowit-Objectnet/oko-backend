package ombruk.backend.vektregistrering.application.api.dto

import arrow.core.Either
import kotlinx.serialization.Serializable
import ombruk.backend.aktor.domain.port.IPartnerRepository
import ombruk.backend.kategori.domain.port.IKategoriRepository
import ombruk.backend.shared.error.ValidationError
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.model.serializer.LocalDateTimeSerializer
import ombruk.backend.shared.utils.validation.allUUIDLegal
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.valiktor.validate
import shared.model.serializer.UUIDSerializer
import java.time.LocalDateTime
import java.util.*

@Serializable
data class VektregistreringBatchSaveDto(
    @Serializable(with = UUIDSerializer::class) val hentingId: UUID,
    val kategoriIds: List<String>,//Serializer library does not allow for serializing list of UUID
    val veiinger: List<Float>
) : IForm<VektregistreringBatchSaveDto>, KoinComponent {
    override fun validOrError(): Either<ValidationError, VektregistreringBatchSaveDto> = runCatchingValidation {
        validate(this) {
            val kategoriRepository: IKategoriRepository by inject()
            val myFun: (UUID) -> Boolean = { transaction { kategoriRepository.findOne(it) } is Either.Right }
            validate(VektregistreringBatchSaveDto::kategoriIds).allUUIDLegal(myFun)
            //TODO: Create a method to check every "veiinger" has a value above 0
        }
    }
}
