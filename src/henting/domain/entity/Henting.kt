package ombruk.backend.henting.domain.entity

import java.time.LocalDateTime
import java.util.*

abstract class Henting {
    abstract val id: UUID
    abstract val startTidspunkt: LocalDateTime
    abstract val sluttTidspunkt: LocalDateTime
    abstract val merknad: String?
}