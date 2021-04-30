package ombruk.backend.avtale.domain.entity

import kotlinx.serialization.Serializable
import ombruk.backend.aktor.domain.entity.Aktor
import ombruk.backend.avtale.model.AvtaleType
import ombruk.backend.henting.domain.entity.Henteplan

@Serializable
data class Avtale(
    val id: Int,
    val aktor: Aktor,
    val type: AvtaleType,
    val henteplaner: List<Henteplan> = emptyList()
)
