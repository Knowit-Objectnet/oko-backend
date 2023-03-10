package ombruk.backend.henting.domain.params

import ombruk.backend.core.domain.model.UpdateParams
import ombruk.backend.henting.domain.model.HenteplanFrekvens
import ombruk.backend.kategori.domain.entity.HenteplanKategori
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.util.*

abstract class HenteplanUpdateParams: UpdateParams {
    abstract override val id: UUID
    abstract val frekvens: HenteplanFrekvens?
    abstract val startTidspunkt: LocalDateTime?
    abstract val sluttTidspunkt: LocalDateTime?
    abstract val ukedag: DayOfWeek?
    abstract val merknad: String?
}