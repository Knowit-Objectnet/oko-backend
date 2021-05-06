package ombruk.backend.henting.domain.entity

import java.time.LocalDateTime
import java.util.*

data class PlanlagtHenting(
    override val id: UUID,
    override val startTidspunkt: LocalDateTime,
    override val sluttTidspunkt: LocalDateTime,
    override val merknad: String?,
    val henteplanId: UUID,
    val avlyst: LocalDateTime?
) : Henting()