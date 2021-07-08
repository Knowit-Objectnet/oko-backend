package ombruk.backend.aarsak.domain.model

import ombruk.backend.core.domain.model.FindParams
import java.util.*

abstract class AarsakFindParams : FindParams {
    abstract override val id: UUID?
    abstract val beskrivelse: String?
}