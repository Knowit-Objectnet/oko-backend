package ombruk.backend.aktor.domain.model

import ombruk.backend.core.domain.model.UpdateParams
import java.util.*

abstract class KontaktVerifiserParams : UpdateParams {
    abstract val telefonKode: String?
    abstract val epostKode: String?
}