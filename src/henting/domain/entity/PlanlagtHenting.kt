package ombruk.backend.henting.domain.entity

import kotlinx.serialization.Serializable
import ombruk.backend.kategori.domain.entity.HenteplanKategori
import ombruk.backend.shared.model.serializer.LocalDateTimeSerializer
import shared.model.serializer.UUIDSerializer
import java.time.LocalDateTime
import java.util.*

@Serializable
data class PlanlagtHenting(
    @Serializable(with = UUIDSerializer::class) override val id: UUID,
    @Serializable(with = LocalDateTimeSerializer::class) override val startTidspunkt: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class) override val sluttTidspunkt: LocalDateTime,
    val merknad: String?,
    @Serializable(with = UUIDSerializer::class) val henteplanId: UUID,
    @Serializable(with = LocalDateTimeSerializer::class) val avlyst: LocalDateTime?,
    @Serializable(with = UUIDSerializer::class) val avlystAv: UUID?,
    @Serializable(with = UUIDSerializer::class) val aarsakId: UUID?,
    @Serializable(with = UUIDSerializer::class) val avtaleId: UUID,
    @Serializable(with = UUIDSerializer::class) val aktorId: UUID,
    val aktorNavn: String,
    @Serializable(with = UUIDSerializer::class) val stasjonId: UUID,
    val stasjonNavn: String,
    val kategorier: List<HenteplanKategori>?
) : Henting()