package ombruk.backend.utlysning.domain.entity

import kotlinx.serialization.Serializable
import shared.model.serializer.UUIDSerializer
import java.util.*

@Serializable
data class Utlysning(
    @Serializable(with = UUIDSerializer::class) val id: UUID,
    @Serializable(with = UUIDSerializer::class) val partnerId: UUID,
    @Serializable(with = UUIDSerializer::class) val hentingId: UUID,
    val partnerPameldt: Boolean,
    val stasjonGodkjent: Boolean,
    val partnerSkjult: Boolean,
    val partnerVist: Boolean
)