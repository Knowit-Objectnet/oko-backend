package ombruk.backend.henting.domain.params

import ombruk.backend.core.domain.model.FindParams
import ombruk.backend.henting.domain.model.HenteplanFrekvens
import java.time.DayOfWeek
import java.time.LocalDateTime

abstract class HenteplanFindParams: FindParams {
    abstract val avtaleId: Int?
    abstract val avtaleIds: List<Int>?
    abstract val stasjonId: Int?
    abstract val frekvens: HenteplanFrekvens?
    abstract val before: LocalDateTime?
    abstract val after: LocalDateTime?
    abstract val ukedag: DayOfWeek?

}