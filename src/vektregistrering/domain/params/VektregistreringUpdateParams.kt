package ombruk.backend.vektregistrering.domain.params

import ombruk.backend.core.domain.model.UpdateParams
import java.util.*

abstract class VektregistreringUpdateParams: UpdateParams {
    abstract val vekt: Float?
}