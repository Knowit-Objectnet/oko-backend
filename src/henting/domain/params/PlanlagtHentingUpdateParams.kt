package ombruk.backend.henting.domain.params

import ombruk.backend.core.domain.model.UpdateParams
import java.time.LocalDateTime
import java.util.*

abstract class PlanlagtHentingUpdateParams : UpdateParams{
    abstract override val id: UUID
    abstract val startTidspunkt: LocalDateTime?
    abstract val sluttTidspunkt: LocalDateTime?
    abstract val merknad: String?
    abstract val avlyst: LocalDateTime?
}