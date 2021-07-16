package ombruk.backend.henting.domain.params

import java.util.*

abstract class HentingWrapperFindParams : HentingFindParams() {
    abstract val stasjonId: UUID?
    abstract val aktorId: UUID?

}