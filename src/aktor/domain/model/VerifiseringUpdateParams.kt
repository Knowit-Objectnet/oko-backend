package ombruk.backend.aktor.domain.model

import ombruk.backend.core.domain.model.UpdateParams

abstract class VerifiseringUpdateParams : UpdateParams {
    abstract val telefonKode: String?
    abstract val telefonVerifisert: Boolean?
    abstract val epostKode: String?
    abstract val epostVerifisert: Boolean?
}