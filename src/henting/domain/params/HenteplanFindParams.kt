package ombruk.backend.henting.domain.params

import ombruk.backend.core.domain.model.FindParams
import ombruk.backend.henting.domain.model.HenteplanFrekvens
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.util.*

abstract class HenteplanFindParams: FindParams {
    abstract val avtaleId: UUID?
    abstract val stasjonId: UUID?
    abstract val frekvens: HenteplanFrekvens?
    abstract val before: LocalDateTime?
    abstract val after: LocalDateTime?
    abstract val ukedag: DayOfWeek?

}