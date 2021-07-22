package ombruk.backend.statistikk.domain.entity

import kotlinx.serialization.Serializable

@Serializable
data class Statistikk(
    val partnere: List<Partner> = listOf(Partner())
)