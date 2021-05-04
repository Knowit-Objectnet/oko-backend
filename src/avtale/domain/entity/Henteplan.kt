package ombruk.backend.avtale.domain.entity

import ombruk.backend.avtale.domain.enum.HenteplanFrekvens
import java.time.LocalDate
import java.time.LocalTime

data class Henteplan(
    val id: Int,
    val avtaleId: Int,
    val stasjonId: Int,
    val frekvens: HenteplanFrekvens,
    val startTidspunkt: LocalTime,
    val sluttTidspunkt: LocalTime,
    val ukeDag: String,
    val startDato: LocalDate,
    val sluttDato: LocalDate,
    val merknad: String
)
