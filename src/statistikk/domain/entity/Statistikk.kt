package ombruk.backend.statistikk.domain.entity

import kotlinx.serialization.Serializable

@Serializable
data class Statistikk(
    val maaned: String,
    val partnere: List<PartnerStatistikk> = emptyList()
)