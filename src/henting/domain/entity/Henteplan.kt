package ombruk.backend.henting.domain.entity

import kotlinx.serialization.Serializable
import ombruk.backend.aktor.domain.entity.Stasjon
import ombruk.backend.avtale.domain.entity.Avtale
import ombruk.backend.henting.domain.model.HenteplanFrekvens
import ombruk.backend.shared.model.serializer.DayOfWeekSerializer
import ombruk.backend.shared.model.serializer.LocalDateTimeSerializer
import ombruk.backend.shared.model.serializer.LocalTimeSerializer
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime

@Serializable
data class Henteplan(
    val id: Int,
    val avtaleId: Int,
    val stasjonId: Int,
    val frekvens: HenteplanFrekvens,
    @Serializable(with = LocalDateTimeSerializer::class) val startTidspunkt: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class) val sluttTidspunkt: LocalDateTime,
    @Serializable(with= DayOfWeekSerializer::class) val ukedag: DayOfWeek,
    var merknad: String?,
    )
