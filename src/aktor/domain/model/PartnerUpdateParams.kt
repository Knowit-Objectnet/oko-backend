package ombruk.backend.aktor.domain.model

import ombruk.backend.core.domain.model.UpdateParams
import java.util.*

abstract class PartnerUpdateParams : UpdateParams {
    abstract override val id: UUID
    abstract val navn: String?
    abstract val ideell: Boolean?
}