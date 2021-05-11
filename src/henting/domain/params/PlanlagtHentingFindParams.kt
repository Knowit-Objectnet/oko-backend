package ombruk.backend.henting.domain.params

import ombruk.backend.core.domain.model.FindParams
import java.time.LocalDateTime
import java.util.*

abstract class PlanlagtHentingFindParams : HentingFindParams(){
    abstract val henteplanId: UUID?
    abstract val avlyst: Boolean? //Find as a boolean, to find any null/non-null
}