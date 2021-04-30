package ombruk.backend.henting.domain.entity

import java.time.LocalDateTime

data class PlanlagtHenting(
    override val id: Int,
    override val startTidspunkt: LocalDateTime,
    override val sluttTidspunkt: LocalDateTime,
    override val merknad: String?,
    val henteplanId: Int,
    val avlyst: LocalDateTime?
) : Henting()