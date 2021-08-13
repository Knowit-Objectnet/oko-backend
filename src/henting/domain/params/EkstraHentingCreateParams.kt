package ombruk.backend.henting.domain.params

import java.util.*

abstract class EkstraHentingCreateParams: HentingCreateParams() {
    abstract val stasjonId: UUID
    abstract val beskrivelse: String
}