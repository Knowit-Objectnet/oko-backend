package ombruk.backend.henting.domain.entity

import java.time.LocalDateTime
import java.util.*

data class InternTransport(
    override val id: UUID,
    override val startTidspunkt: LocalDateTime,
    override val sluttTidspunkt: LocalDateTime,
    override val merknad: String?,
    val mottakerId: UUID,
    val avsenderId: UUID,
) : Henting()