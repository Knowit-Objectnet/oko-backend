package ombruk.backend.avtale.domain.params

import ombruk.backend.avtale.model.AvtaleType
import ombruk.backend.core.domain.model.FindParams

abstract class AvtaleFindParams: FindParams {
    abstract val aktorId: Int?
    abstract val type: AvtaleType?
}