package ombruk.backend.aarsak.domain.model

import ombruk.backend.aarsak.domain.enum.AarsakType
import ombruk.backend.core.domain.model.UpdateParams
import java.util.*

abstract class AarsakUpdateParams : UpdateParams {
    abstract override val id: UUID
    abstract val beskrivelse: String?
    abstract val type: AarsakType?
}