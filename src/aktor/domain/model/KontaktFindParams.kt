package ombruk.backend.aktor.domain.model

import ombruk.backend.core.domain.model.FindParams

abstract class KontaktFindParams : FindParams {
    abstract override val id: Int
    abstract val navn: String
    abstract val telefon: String
    abstract val rolle: String
}