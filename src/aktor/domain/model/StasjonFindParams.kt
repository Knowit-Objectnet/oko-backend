package ombruk.backend.aktor.domain.model

import ombruk.backend.aktor.domain.enum.StasjonType
import ombruk.backend.core.domain.model.FindParams
import java.util.*

abstract class StasjonFindParams : FindParams {
    abstract override val id: UUID?
    abstract val navn: String?
    abstract val type: StasjonType?
}