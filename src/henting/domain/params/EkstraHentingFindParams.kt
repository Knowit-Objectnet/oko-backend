package ombruk.backend.henting.domain.params

import java.util.*

abstract class EkstraHentingFindParams : HentingFindParams(){
    abstract val stasjonId: UUID?
}