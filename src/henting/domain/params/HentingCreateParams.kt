package ombruk.backend.henting.domain.params

import java.time.LocalDateTime
import java.util.*

abstract class HentingCreateParams {
    abstract val startTidspunkt: LocalDateTime
    abstract val sluttTidspunkt: LocalDateTime
    abstract val merknad: String?
}