package ombruk.backend.henting.domain.params

import ombruk.backend.core.domain.model.FindParams
import java.time.LocalDateTime
import java.util.*

abstract class PlanlagtHentingFindParams : FindParams{
    abstract val after: LocalDateTime? //Find any henting starting after this time
    abstract val before: LocalDateTime? //Find any henting ending before this time
    abstract val merknad: String? //Does it make sense finding based on merknad? Current: Merknad contains the string
    abstract val henteplanId: UUID?
    abstract val avlyst: Boolean? //Find as a boolean, to find any null/non-null
}