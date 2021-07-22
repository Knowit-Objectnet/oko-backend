package ombruk.backend.statistikk.domain.entity

import kotlinx.serialization.Serializable
import shared.model.serializer.UUIDSerializer
import java.util.*

@Serializable
data class Stasjon(
    val stasjonNavn: String = "test",
    val kategorier: List<Kategori> = listOf(Kategori())
)