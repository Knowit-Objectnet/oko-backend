package ombruk.backend.utlysning.domain.params

import java.time.LocalDateTime
import java.util.*

abstract class UtlysningPartnerAcceptParams {
    abstract val id: UUID
    abstract val toAccept: Boolean
}