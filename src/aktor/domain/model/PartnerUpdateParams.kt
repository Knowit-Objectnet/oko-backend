package ombruk.backend.aktor.domain.model

import ombruk.backend.aktor.domain.enum.PartnerStorrelse
import ombruk.backend.core.domain.model.UpdateParams

abstract class PartnerUpdateParams : UpdateParams {
    abstract override val id: Int
    abstract val navn: String?
    abstract val ideell: Boolean?
    abstract val storrelse: PartnerStorrelse?
}