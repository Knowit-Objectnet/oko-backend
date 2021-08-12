package ombruk.backend.henting.domain.entity

import kotlinx.serialization.Serializable
import ombruk.backend.henting.domain.model.HentingType
import ombruk.backend.shared.model.serializer.LocalDateTimeSerializer
import shared.model.serializer.UUIDSerializer
import java.time.LocalDateTime
import java.util.*

@Serializable
data class HentingWrapper(
    @Serializable(with = UUIDSerializer::class) val id: UUID,
    @Serializable(with = LocalDateTimeSerializer::class) val startTidspunkt: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class) val sluttTidspunkt: LocalDateTime,
    val type: HentingType,
    val planlagtHenting: PlanlagtHenting? = null,
    val ekstraHenting: EkstraHenting? = null,
    @Serializable(with = UUIDSerializer::class) val stasjonId: UUID,
    val stasjonNavn: String,
    @Serializable(with = UUIDSerializer::class) val aktorId: UUID? = null,
    val aktorNavn: String? = null
)