package ombruk.backend.henting.domain.entity

import ombruk.backend.vektregistrering.domain.entity.Vektregistrering
import java.time.LocalDateTime
import java.util.*

abstract class Henting {
    abstract val id: UUID
    abstract val startTidspunkt: LocalDateTime
    abstract val sluttTidspunkt: LocalDateTime
    abstract val vektregistreringer: List<Vektregistrering>?
}