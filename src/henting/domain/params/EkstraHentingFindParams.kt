package ombruk.backend.henting.domain.params

import java.util.*

abstract class EkstraHentingFindParams : HentingFindParams(){
    abstract val stasjonId: UUID?
    abstract val beskrivelse: String?
    abstract val aktorId: UUID?
}