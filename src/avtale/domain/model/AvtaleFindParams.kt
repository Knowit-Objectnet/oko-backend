package ombruk.backend.avtale.domain.model

import ombruk.backend.avtale.domain.enum.AvtaleType
import ombruk.backend.core.domain.model.FindParams

abstract class AvtaleFindParams : FindParams {
    abstract val aktorId: Int?
    abstract val type: AvtaleType?
}