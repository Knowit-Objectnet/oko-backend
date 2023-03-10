package ombruk.backend.henting.domain.params

import ombruk.backend.core.domain.model.UpdateParams
import java.time.LocalDateTime
import java.util.*

abstract class PlanlagtHentingUpdateParams : HentingUpdateParams(){
    abstract val avlyst: Boolean? //Should this instead be a separate avlys-all?
    abstract val aarsakId: UUID?
}