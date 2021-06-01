package ombruk.backend.kategori.domain.params

import ombruk.backend.core.domain.model.FindParams

abstract class KategoriFindParams : FindParams {
    abstract val navn: String?
}