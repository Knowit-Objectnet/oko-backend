package ombruk.backend.henting.domain.params

import ombruk.backend.core.domain.model.UpdateParams
import ombruk.backend.henting.domain.model.HenteplanFrekvens
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

abstract class HenteplanUpdateParams: UpdateParams {
    abstract override val id: Int
    abstract val frekvens: HenteplanFrekvens?
    abstract val startTidspunkt: LocalDateTime?
    abstract val sluttTidspunkt: LocalDateTime?
    abstract val ukeDag: DayOfWeek?
    abstract val merknad: String?
}