package ombruk.backend.aktor.domain.model

import ombruk.backend.aktor.domain.enum.StasjonType
import ombruk.backend.core.domain.model.UpdateParams

abstract class StasjonUpdateParams : UpdateParams {
    abstract override val id: Int
    abstract val navn: String?
    abstract val type: StasjonType?
}