package ombruk.backend.henting.domain.params

import ombruk.backend.henting.domain.model.HenteplanFrekvens
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime

abstract class HenteplanCreateParams {
    abstract val avtaleId: Int? // for adding new henteplan to an existing avtale.
    abstract val stasjonId: Int
    abstract val frekvens: HenteplanFrekvens
    abstract val startTidspunkt: LocalDateTime
    abstract val sluttTidspunkt: LocalDateTime
    abstract val ukedag: DayOfWeek
    abstract val merknad: String?
}