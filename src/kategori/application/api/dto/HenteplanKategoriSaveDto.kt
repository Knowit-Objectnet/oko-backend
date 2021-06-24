package ombruk.backend.kategori.application.api.dto

import arrow.core.Either
import kotlinx.serialization.Serializable
import ombruk.backend.henting.domain.port.IHenteplanRepository
import ombruk.backend.kategori.domain.params.HenteplanKategoriCreateParams
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
data class HenteplanKategoriSaveDto(
    @Serializable(with = UUIDSerializer::class) override val henteplanId: UUID,
    @Serializable(with = UUIDSerializer::class) override val kategoriId: UUID,
    override val merknad: String? = null
) : IForm<HenteplanKategoriSaveDto>, HenteplanKategoriCreateParams(), KoinComponent {
    override fun validOrError(): Either<ValidationError, HenteplanKategoriSaveDto> = runCatchingValidation {
        validate(this) {
            val kategoriRepository: IKategoriRepository by inject()
            val kategoriExist: (UUID) -> Boolean = { transaction { kategoriRepository.findOne(it) } is Either.Right }
            validate(HenteplanKategoriSaveDto::kategoriId).isExistingUUID({ kategoriExist(it) } , UUIDKategori)

            val henteplanRepository: IHenteplanRepository by inject()
            val henteplanExist: (UUID) -> Boolean = { transaction { henteplanRepository.findOne(it) } is Either.Right }
            validate(HenteplanKategoriSaveDto::henteplanId).isExistingUUID({ henteplanExist(it) }, UUIDHenteplan)
        }
    }
}