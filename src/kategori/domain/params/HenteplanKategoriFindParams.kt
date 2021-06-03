package ombruk.backend.kategori.domain.params

import ombruk.backend.core.domain.model.FindParams
import java.util.*

abstract class HenteplanKategoriFindParams : FindParams {
    abstract val henteplanId: UUID?
    abstract val kategoriId: UUID?
    abstract val merknad: String?
}