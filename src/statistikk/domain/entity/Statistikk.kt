package ombruk.backend.statistikk.domain.entity

import kotlinx.serialization.Serializable

@Serializable
data class Statistikk(
    val partnerNavn: String = "test",
    val stasjoner: List<Stasjon> = emptyList()
)