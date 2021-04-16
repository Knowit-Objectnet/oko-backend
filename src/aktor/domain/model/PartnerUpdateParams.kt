package ombruk.backend.aktor.domain.model

import ombruk.backend.aktor.domain.enum.PartnerStorrelse

abstract class PartnerUpdateParams {
    abstract val id: Int
    abstract val navn: String?
    abstract val ideell: Boolean?
    abstract val storrelse: PartnerStorrelse?
}