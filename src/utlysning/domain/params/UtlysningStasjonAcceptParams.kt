package ombruk.backend.utlysning.domain.params

import java.time.LocalDateTime
import java.util.*

abstract class UtlysningStasjonAcceptParams {
    abstract val id: UUID
    abstract val toAccept: Boolean
}