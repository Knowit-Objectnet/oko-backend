package ombruk.backend.aktor.domain.model

import ombruk.backend.core.domain.model.FindParams
import java.util.*

abstract class KontaktFindParams : FindParams {
    abstract override val id: UUID
    abstract val navn: String
    abstract val telefon: String?
    abstract val rolle: String?
}