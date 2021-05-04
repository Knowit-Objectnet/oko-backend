package ombruk.backend.avtale.application.api.dto

import arrow.core.Either
import arrow.core.invalid
import io.ktor.locations.*
import kotlinx.serialization.Serializable
import ombruk.backend.avtale.domain.params.AvtaleFindParams
import ombruk.backend.avtale.model.AvtaleType
import ombruk.backend.shared.error.ValidationError
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.model.serializer.LocalDateSerializer
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.ConstraintViolationException
import org.valiktor.functions.isPositive
import org.valiktor.functions.isValid
import org.valiktor.validate
import shared.model.serializer.UUIDSerializer
import java.lang.Exception
import java.time.LocalDate
import java.util.*

@Location("/")
@Serializable
data class AvtaleFindDto(
    @Serializable( with = UUIDSerializer::class)
    override val aktorId: String?,
    override val type: AvtaleType?,
    override val id: Int?,
    @Serializable( with = LocalDateSerializer::class)
    override val startDato: LocalDate,
    @Serializable( with = LocalDateSerializer::class)
    override val sluttDato: LocalDate
) : IForm<AvtaleFindDto>, AvtaleFindParams() {
    override fun validOrError(): Either<ValidationError, AvtaleFindDto> = runCatchingValidation {
        validate(this) {
            aktorId?.let { validate(AvtaleFindDto::aktorId).isValid {
                    try {
                        UUID.fromString(it)
                        return@isValid true
                    }
                    catch (e: Exception) {
                        return@isValid false
                    }
                }
            }
            id?.let { validate(AvtaleFindDto::id).isPositive() }
        }
    }
}