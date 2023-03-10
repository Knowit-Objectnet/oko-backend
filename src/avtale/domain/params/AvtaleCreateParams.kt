package ombruk.backend.avtale.domain.params

import ombruk.backend.avtale.model.AvtaleType
import ombruk.backend.henting.domain.params.HenteplanCreateParams
import java.time.LocalDate
import java.util.*

abstract class AvtaleCreateParams {
    abstract val aktorId: UUID
    abstract val type: AvtaleType
    abstract val startDato: LocalDate
    abstract val sluttDato: LocalDate
    abstract val henteplaner: List<HenteplanCreateParams>?
    abstract val saksnummer: String?
}