package ombruk.backend.avtale.model.Avtale

import kotlinx.serialization.Serializable
import ombruk.backend.aktor.domain.entity.Stasjon
import ombruk.backend.shared.model.serializer.LocalTimeSerializer
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime

enum class SubAvtaleFrekvens {
    ENKELT,
    UKENTLIG,
    ANNENHVER_UKE
}

data class SubAvtale(
    val id: Int,
    val avtaleId: Int,
    val stasjon: Stasjon,
    val frekvens: SubAvtaleFrekvens,
    @Serializable(with = LocalTimeSerializer::class) val startTidspunkt: LocalTime,
    @Serializable(with = LocalTimeSerializer::class) val sluttTidspunkt: LocalTime,
    val ukeDag: DayOfWeek,
    val startDato: LocalDateTime,
    val sluttDato: LocalDateTime,
    val merknad: String
)