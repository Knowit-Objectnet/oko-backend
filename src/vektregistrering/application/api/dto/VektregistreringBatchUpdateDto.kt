package ombruk.backend.vektregistrering.application.api.dto

import arrow.core.Either
import kotlinx.serialization.Serializable
import ombruk.backend.henting.application.service.IHentingService
import ombruk.backend.shared.error.ValidationError
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.valiktor.functions.isValid
import org.valiktor.validate
import shared.model.serializer.UUIDSerializer
import java.util.*

@Serializable
data class VektregistreringBatchUpdateDto(
    @Serializable(with = UUIDSerializer::class) val hentingId: UUID,
    val vektregistreringIds: List<String> = emptyList(),//Serializer library does not allow for serializing list of UUID
    val veiinger: List<Float> = emptyList()
) : IForm<VektregistreringBatchUpdateDto>, KoinComponent {
    override fun validOrError(): Either<ValidationError, VektregistreringBatchUpdateDto> = runCatchingValidation {
        validate(this) {
            val hentingService: IHentingService by inject()
            val hentingExist: (UUID) -> Boolean = { transaction { hentingService.findOne(it) } is Either.Right }
            validate(VektregistreringBatchUpdateDto::hentingId).isExistingUUID({ hentingExist(it) }, UUIDGenerelt)

            validate(VektregistreringBatchUpdateDto::veiinger).isPositiveOrZeroList()
            validate(VektregistreringBatchUpdateDto::veiinger).equalSizeOfIDList(vektregistreringIds)
        }
    }
}
