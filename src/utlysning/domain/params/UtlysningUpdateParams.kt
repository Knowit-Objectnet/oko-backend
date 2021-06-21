package ombruk.backend.utlysning.domain.params

import ombruk.backend.core.domain.model.UpdateParams
import java.time.LocalDateTime
import java.util.*

abstract class UtlysningUpdateParams : UpdateParams {
    abstract val partnerSkjult: Boolean?
    abstract val partnerVist: Boolean?
}