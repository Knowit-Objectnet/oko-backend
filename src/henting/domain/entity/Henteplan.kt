package ombruk.backend.henting.domain.entity

import kotlinx.serialization.Serializable
import ombruk.backend.henting.domain.model.HenteplanFrekvens
import ombruk.backend.kategori.domain.entity.HenteplanKategori
import ombruk.backend.shared.model.serializer.DayOfWeekSerializer
import ombruk.backend.shared.model.serializer.LocalDateTimeSerializer
import shared.model.serializer.UUIDSerializer
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.util.*

@Serializable
data class Henteplan(
    @Serializable(with = UUIDSerializer::class) val id: UUID,
    @Serializable(with = UUIDSerializer::class) val avtaleId: UUID,
    @Serializable(with = UUIDSerializer::class) val stasjonId: UUID,
    val frekvens: HenteplanFrekvens,
    @Serializable(with = LocalDateTimeSerializer::class) val startTidspunkt: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class) val sluttTidspunkt: LocalDateTime,
    @Serializable(with= DayOfWeekSerializer::class) val ukedag: DayOfWeek?,
    var merknad: String?,
    val planlagteHentinger: List<PlanlagtHenting>?,
    val kategorier: List<HenteplanKategori>?
    )
