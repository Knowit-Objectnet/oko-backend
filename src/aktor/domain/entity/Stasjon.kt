package ombruk.backend.aktor.domain.entity

import ombruk.backend.aktor.domain.enum.StasjonType

data class Stasjon (
    override val id: Int,
    override val navn: String,
    override val kontaktPersoner: List<Kontakt>,
    val type: StasjonType
): Aktor