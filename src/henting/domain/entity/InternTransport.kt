package ombruk.backend.henting.domain.entity

import java.time.LocalDateTime

data class InternTransport(
    override val id: Int,
    override val startTidspunkt: LocalDateTime,
    override val sluttTidspunkt: LocalDateTime,
    override val merknad: String?,
    val mottakerId: Int,
    val avsenderId: Int,
) : Henting()