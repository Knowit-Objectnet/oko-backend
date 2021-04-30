package ombruk.backend.avtale.domain.params

import ombruk.backend.avtale.model.AvtaleType
import ombruk.backend.henting.domain.params.HenteplanCreateParams

abstract class AvtaleCreateParams {
    abstract val aktorId: Int
    abstract val type: AvtaleType
    abstract val henteplaner: List<HenteplanCreateParams>
}