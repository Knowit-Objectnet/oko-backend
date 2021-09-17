package ombruk.backend.vektregistrering.application.api.dto

import arrow.core.Either
import kotlinx.serialization.Serializable
import ombruk.backend.henting.application.service.IHentingService
import ombruk.backend.kategori.domain.port.IKategoriRepository
import ombruk.backend.shared.error.ValidationError
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.*
import ombruk.backend.vektregistrering.domain.params.VektregistreringCreateParams
import ombruk.backend.vektregistrering.domain.params.VektregistreringUpdateParams
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.valiktor.functions.isPositiveOrZero
import org.valiktor.validate
import shared.model.serializer.UUIDSerializer
import java.util.*

@Serializable
data class VektregistreringUpdateDto(
    @Serializable(with = UUIDSerializer::class) override val id: UUID,
    @Serializable(with = UUIDSerializer::class) val hentingId: UUID,
    override val vekt: Float? = null,
    @Serializable(with = UUIDSerializer::class) override val vektRegistreringAv: UUID? = null
) : IForm<VektregistreringUpdateDto>, VektregistreringUpdateParams(), KoinComponent {
    override fun validOrError(): Either<ValidationError, VektregistreringUpdateDto> = runCatchingValidation {
        validate(this) {
            val hentingService: IHentingService by inject()
            val hentingExist: (UUID) -> Boolean = { transaction { hentingService.findOne(it) } is Either.Right }
            validate(VektregistreringUpdateDto::hentingId).isExistingUUID({ hentingExist(it) }, UUIDGenerelt)
            vekt.let { validate(VektregistreringUpdateDto::vekt).isPositiveOrZero() }
        }
    }
}