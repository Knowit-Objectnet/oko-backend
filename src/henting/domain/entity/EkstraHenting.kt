package ombruk.backend.henting.domain.entity

import kotlinx.serialization.Serializable
import ombruk.backend.shared.model.serializer.LocalDateTimeSerializer
import shared.model.serializer.UUIDSerializer
import java.time.LocalDateTime
import java.util.*

@Serializable
data class EkstraHenting(
    @Serializable(with = UUIDSerializer::class) override val id: UUID,
    @Serializable(with = LocalDateTimeSerializer::class) override val startTidspunkt: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class) override val sluttTidspunkt: LocalDateTime,
    override val merknad: String?,
    @Serializable(with = UUIDSerializer::class) val stasjonId: UUID,
) : Henting()