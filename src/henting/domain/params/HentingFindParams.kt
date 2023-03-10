package ombruk.backend.henting.domain.params

import ombruk.backend.core.domain.model.FindParams
import java.time.LocalDateTime
import java.util.*

abstract class HentingFindParams : FindParams{
    abstract val after: LocalDateTime? //Find any henting starting after this time
    abstract val before: LocalDateTime? //Find any henting ending before this time
}