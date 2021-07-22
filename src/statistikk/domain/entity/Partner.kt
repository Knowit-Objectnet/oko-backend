package ombruk.backend.statistikk.domain.entity

import kotlinx.serialization.Serializable

@Serializable
data class Partner(
    val partnerNavn: String = "test",
    val stasjoner: List<Stasjon> = listOf(Stasjon())
)