package ombruk.backend.vektregistrering.domain.entity

import kotlinx.serialization.Serializable
import ombruk.backend.shared.model.serializer.LocalDateTimeSerializer
import shared.model.serializer.UUIDSerializer
import java.time.LocalDateTime
import java.util.*

@Serializable
data class Vektregistrering(
    @Serializable(with = UUIDSerializer::class) val id: UUID,
    @Serializable(with = UUIDSerializer::class) val hentingId: UUID,
    @Serializable(with = UUIDSerializer::class) val kategoriId: UUID,
    val vekt: Float,
    @Serializable(with = LocalDateTimeSerializer::class) val registreringsDato: LocalDateTime
)