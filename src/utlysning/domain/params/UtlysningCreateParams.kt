package ombruk.backend.utlysning.domain.params

import java.time.LocalDateTime
import java.util.*

abstract class UtlysningCreateParams {
    abstract val partnerId: UUID
    abstract val hentingId: UUID
    abstract val partnerPameldt: LocalDateTime?
    abstract val stasjonGodkjent: LocalDateTime?
    abstract val partnerSkjult: Boolean
    abstract val partnerVist: Boolean
}