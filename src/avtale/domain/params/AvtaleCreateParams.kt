package ombruk.backend.avtale.domain.params

import ombruk.backend.avtale.model.AvtaleType
import ombruk.backend.henting.domain.params.HenteplanCreateParams
import java.util.*

abstract class AvtaleCreateParams {
    abstract val aktorId: UUID
    abstract val type: AvtaleType
    abstract val henteplaner: List<HenteplanCreateParams>
}