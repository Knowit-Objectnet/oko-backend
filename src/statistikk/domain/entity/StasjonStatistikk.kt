package ombruk.backend.statistikk.domain.entity

import kotlinx.serialization.Serializable

@Serializable
data class StasjonStatistikk(
    val stasjonNavn: String = "test",
    val kategorier: List<KategoriStatistikk> = emptyList()
)