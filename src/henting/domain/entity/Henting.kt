package ombruk.backend.henting.domain.entity

import java.time.LocalDateTime

abstract class Henting {
    abstract val id: Int
    abstract val startTidspunkt: LocalDateTime
    abstract val sluttTidspunkt: LocalDateTime
    abstract val merknad: String?
}