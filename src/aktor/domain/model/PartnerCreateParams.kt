package ombruk.backend.aktor.domain.model

import ombruk.backend.aktor.domain.enum.PartnerStorrelse

abstract class PartnerCreateParams {
    abstract val navn: String
    abstract val storrelse: PartnerStorrelse
    abstract val ideell: Boolean
}