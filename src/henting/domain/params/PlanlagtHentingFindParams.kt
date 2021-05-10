package ombruk.backend.henting.domain.params

import ombruk.backend.core.domain.model.FindParams
import java.time.LocalDateTime
import java.util.*

abstract class PlanlagtHentingFindParams : FindParams{
    abstract val startTidspunkt: LocalDateTime?
    abstract val sluttTidspunkt: LocalDateTime?
    abstract val merknad: String?
    abstract val henteplanId: UUID?
    abstract val avlyst: LocalDateTime?
}