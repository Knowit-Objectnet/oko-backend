package ombruk.backend.aktor.domain.model

import ombruk.backend.core.domain.model.UpdateParams
import java.util.*

abstract class KontaktUpdateParams : UpdateParams {
    abstract override val id: UUID
    abstract val navn: String
    abstract val telefon: String?
    abstract val rolle: String?
}