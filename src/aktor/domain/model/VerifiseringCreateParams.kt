package ombruk.backend.aktor.domain.model

import ombruk.backend.core.domain.model.CreateParams
import java.util.*


abstract class VerifiseringCreateParams : CreateParams {
    abstract val id: UUID
    abstract val telefonKode: String?
    abstract val telefonVerifisert: Boolean?
    abstract val epostKode: String?
    abstract val epostVerifisert: Boolean?
}