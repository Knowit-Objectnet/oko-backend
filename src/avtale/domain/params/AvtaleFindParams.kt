package ombruk.backend.avtale.domain.params

import ombruk.backend.avtale.model.AvtaleType
import ombruk.backend.core.domain.model.FindParams
import java.util.*

abstract class AvtaleFindParams: FindParams {
    abstract val aktorId: UUID?
    abstract val type: AvtaleType?
}