package ombruk.backend.utlysning.domain.params

import ombruk.backend.core.domain.model.UpdateParams
import java.util.*

abstract class UtlysningUpdateParams : UpdateParams {
    abstract val partnerId: UUID?
    abstract val hentingId: UUID?
    abstract val partnerPameldt: Boolean?
    abstract val stasjonGodkjent: Boolean?
    abstract val partnerSkjult: Boolean?
    abstract val partnerVist: Boolean?
}