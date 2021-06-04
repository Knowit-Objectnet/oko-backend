package ombruk.backend.utlysning.domain.entity

import kotlinx.serialization.Serializable
import ombruk.backend.shared.model.serializer.LocalDateTimeSerializer
import shared.model.serializer.UUIDSerializer
import java.time.LocalDateTime
import java.util.*

@Serializable
data class Utlysning(
    @Serializable(with = UUIDSerializer::class) val id: UUID,
    @Serializable(with = UUIDSerializer::class) val partnerId: UUID,
    @Serializable(with = UUIDSerializer::class) val hentingId: UUID,
    @Serializable(with = LocalDateTimeSerializer::class) val partnerPameldt: LocalDateTime?,
    @Serializable(with = LocalDateTimeSerializer::class) val stasjonGodkjent: LocalDateTime?,
    val partnerSkjult: Boolean,
    val partnerVist: Boolean
)