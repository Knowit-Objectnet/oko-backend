package ombruk.backend.aktor.domain.model

import ombruk.backend.aktor.domain.enum.PartnerStorrelse

abstract class PartnerFindParams(
    open val navn: String? = null,
    open val storrelse: PartnerStorrelse? = null,
    open val ideell: Boolean? = null
)