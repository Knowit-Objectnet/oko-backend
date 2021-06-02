package ombruk.backend.utlysning.domain.params

import ombruk.backend.core.domain.model.FindParams
import java.time.LocalDateTime
import java.util.*

abstract class UtlysningFindParams : FindParams {
    abstract val partnerId: UUID?
    abstract val hentingId: UUID?
    abstract val partnerPameldt: LocalDateTime?
    abstract val stasjonGodkjent: LocalDateTime?
    abstract val partnerSkjult: Boolean?
    abstract val partnerVist: Boolean?
}