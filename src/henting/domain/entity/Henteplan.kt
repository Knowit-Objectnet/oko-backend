package ombruk.backend.henting.domain.entity

import kotlinx.serialization.Serializable
import ombruk.backend.aktor.domain.entity.Stasjon
import ombruk.backend.avtale.domain.entity.Avtale
import ombruk.backend.henting.domain.model.HenteplanFrekvens
import ombruk.backend.shared.model.serializer.DayOfWeekSerializer
import ombruk.backend.shared.model.serializer.LocalDateTimeSerializer
import ombruk.backend.shared.model.serializer.LocalTimeSerializer
import shared.model.serializer.UUIDSerializer
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

@Serializable(with = UUIDSerializer::class)
data class Henteplan(
    val id: UUID,
    val avtaleId: UUID,
    val stasjonId: UUID,
    val frekvens: HenteplanFrekvens,
    @Serializable(with = LocalDateTimeSerializer::class) val startTidspunkt: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class) val sluttTidspunkt: LocalDateTime,
    @Serializable(with= DayOfWeekSerializer::class) val ukedag: DayOfWeek,
    var merknad: String?,
    )
