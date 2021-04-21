package ombruk.backend.aktor.domain.model

import ombruk.backend.aktor.domain.enum.PartnerStorrelse

abstract class KontaktFindParams(
    open val navn: String? = null,
    open val telefon: String? = null,
    open val rolle: String? = null
)