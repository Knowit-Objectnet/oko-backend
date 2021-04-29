package ombruk.backend.avtale.domain.model

import ombruk.backend.avtale.domain.enum.AvtaleType
import ombruk.backend.core.domain.model.FindParams
import ombruk.backend.core.domain.model.UpdateParams

abstract class AvtaleUpdateParams : UpdateParams {
    abstract override val id: Int
    abstract val aktorId: Int?
    abstract val type: AvtaleType?
}