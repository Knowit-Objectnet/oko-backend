package ombruk.backend.vektregistrering.domain.params

import java.util.*

abstract class VektregistreringCreateParams {
    abstract val hentingId: UUID
    abstract val kategoriId: UUID
    abstract val vekt: Float
    abstract val vektRegistreringAv: UUID?
}