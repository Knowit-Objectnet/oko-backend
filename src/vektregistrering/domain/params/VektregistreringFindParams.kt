package ombruk.backend.vektregistrering.domain.params

import ombruk.backend.core.domain.model.FindParams
import java.time.LocalDateTime
import java.util.*

abstract class VektregistreringFindParams : FindParams {
    abstract val hentingId: UUID?
    abstract val kategoriId: UUID?
    abstract val vekt: Float?
    abstract val before: LocalDateTime?
    abstract val after: LocalDateTime?
    abstract val vektRegistreringAv: UUID?
}