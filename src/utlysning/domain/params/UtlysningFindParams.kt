package ombruk.backend.utlysning.domain.params

import ombruk.backend.core.domain.model.FindParams
import java.util.*

abstract class UtlysningFindParams : FindParams {
    abstract val partnerId: UUID?
    abstract val hentingId: UUID?
    abstract val partnerPameldt: Boolean?
    abstract val stasjonGodkjent: Boolean?
    abstract val partnerSkjult: Boolean?
    abstract val partnerVist: Boolean?
}