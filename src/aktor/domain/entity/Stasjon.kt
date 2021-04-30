package ombruk.backend.aktor.domain.entity

import kotlinx.serialization.Serializable
import ombruk.backend.aktor.domain.enum.StasjonType

@Serializable
data class Stasjon (
    override val id: Int,
    override var navn: String,
    override var kontaktPersoner: List<Kontakt>,
    val type: StasjonType
): Aktor