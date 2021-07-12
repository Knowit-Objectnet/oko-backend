package ombruk.backend.aktor.domain.model

import ombruk.backend.core.domain.model.FindParams

abstract class VerifiseringFindParams : FindParams {
    abstract val telefonKode: String?
    abstract val telefonVerifisert: Boolean
    abstract val epostKode: String?
    abstract val epostVerifisert: Boolean
}