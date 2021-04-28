package ombruk.backend.aktor.domain.model

import ombruk.backend.aktor.domain.enum.StasjonType
import ombruk.backend.core.domain.model.CreateParams

abstract class StasjonCreateParams : CreateParams {
    abstract val navn: String
    abstract val type: StasjonType
}