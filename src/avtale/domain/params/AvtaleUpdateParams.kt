package ombruk.backend.avtale.domain.params

import ombruk.backend.avtale.model.AvtaleType
import ombruk.backend.core.domain.model.UpdateParams
import ombruk.backend.henting.domain.params.HenteplanUpdateParams
import java.time.LocalDate
import java.util.*

abstract class AvtaleUpdateParams: UpdateParams{
    abstract override val id: Int
    abstract val type: AvtaleType?
    abstract val startDato: LocalDate?
    abstract val sluttDato: LocalDate?
//    abstract val henteplaner: List<HenteplanUpdateParams>
}