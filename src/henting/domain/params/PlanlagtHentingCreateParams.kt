package ombruk.backend.henting.domain.params

import java.time.LocalDateTime
import java.util.*

abstract class PlanlagtHentingCreateParams: HentingCreateParams() {
    abstract val henteplanId: UUID
}