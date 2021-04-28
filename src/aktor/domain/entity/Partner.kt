package ombruk.backend.aktor.domain.entity

import ombruk.backend.aktor.domain.enum.PartnerStorrelse


data class Partner (
    override var id: Int = 0,
    override var navn: String,
    override var kontaktPersoner: List<Kontakt> = emptyList(),
    var storrelse: PartnerStorrelse,
    var ideell: Boolean
): Aktor