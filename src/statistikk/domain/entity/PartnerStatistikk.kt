package ombruk.backend.statistikk.domain.entity

import kotlinx.serialization.Serializable

@Serializable
data class PartnerStatistikk(
    val partnerNavn: String = "test",
    val stasjoner: List<StasjonStatistikk> = emptyList()
)