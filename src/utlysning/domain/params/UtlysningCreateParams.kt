package ombruk.backend.utlysning.domain.params

import java.util.*

abstract class UtlysningCreateParams {
    abstract val partnerId: UUID
    abstract val hentingId: UUID
    abstract val partnerPameldt: Boolean
    abstract val stasjonGodkjent: Boolean
    abstract val partnerSkjult: Boolean
    abstract val partnerVist: Boolean
}