package ombruk.backend.aarsak.domain.model

import ombruk.backend.aarsak.domain.enum.AarsakType
import ombruk.backend.core.domain.model.CreateParams

abstract class AarsakCreateParams : CreateParams {
    abstract val beskrivelse: String
    abstract val type: AarsakType?
}