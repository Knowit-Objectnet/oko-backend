package ombruk.backend.henting.domain.entity

import kotlinx.serialization.Serializable
import ombruk.backend.shared.model.serializer.LocalDateTimeSerializer
import shared.model.serializer.UUIDSerializer
import java.time.LocalDateTime
import java.util.*

@Serializable
data class PlanlagtHenting(
    @Serializable(with = UUIDSerializer::class) override val id: UUID,
    @Serializable(with = LocalDateTimeSerializer::class) override val startTidspunkt: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class) override val sluttTidspunkt: LocalDateTime,
    override val merknad: String?,
    @Serializable(with = UUIDSerializer::class) val henteplanId: UUID,
    @Serializable(with = LocalDateTimeSerializer::class) val avlyst: LocalDateTime?
) : Henting()

data class PlanlagtHentingWithParents(
    override val id: UUID,
    override val startTidspunkt: LocalDateTime,
    override val sluttTidspunkt: LocalDateTime,
    override val merknad: String?,
    val henteplanId: UUID,
    val avlyst: LocalDateTime?,
    val avtaleId: UUID,
//    val aktorId: UUID,
//    val aktorName: String,
    val stasjonId: UUID,
    val stasjonNavn: String
) : Henting()