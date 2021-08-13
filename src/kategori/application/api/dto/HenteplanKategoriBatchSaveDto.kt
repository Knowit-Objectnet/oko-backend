package ombruk.backend.kategori.application.api.dto

import arrow.core.Either
import arrow.core.right
import kotlinx.serialization.Serializable
import ombruk.backend.kategori.domain.entity.Kategori
import ombruk.backend.kategori.domain.params.HenteplanKategoriCreateParams
import ombruk.backend.kategori.domain.port.IKategoriRepository
import ombruk.backend.shared.error.RepositoryError
import ombruk.backend.shared.error.ValidationError
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.valiktor.functions.isEqualTo
import org.valiktor.functions.isValid
import org.valiktor.validate
import shared.model.serializer.UUIDSerializer
import java.util.*


@Serializable
data class HenteplanKategoriBatchSaveDto(
    @Serializable(with = UUIDSerializer::class) override val kategoriId: UUID
) : IForm<HenteplanKategoriBatchSaveDto>, IKategoriKoblingSaveDto, KoinComponent {
    override fun validOrError(): Either<ValidationError, HenteplanKategoriBatchSaveDto> = runCatchingValidation {
        validate(this) {
        }
    }
}