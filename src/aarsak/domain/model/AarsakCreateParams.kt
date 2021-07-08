package ombruk.backend.aarsak.domain.model

import ombruk.backend.aarsak.domain.enum.AarsakType
import ombruk.backend.core.domain.model.CreateParams
import java.util.*


abstract class AarsakCreateParams : CreateParams {
    abstract val id: UUID?
    abstract val beskrivelse: String
    abstract val type: AarsakType
}