package ombruk.backend.avtale.domain.model

import ombruk.backend.avtale.domain.enum.HenteplanFrekvens
import ombruk.backend.core.domain.model.FindParams
import java.time.LocalDate
import java.time.LocalTime

abstract class HenteplanCreateParams {
    abstract val avtaleId: Int
    abstract val stasjonId: Int
    abstract val frekvens: HenteplanFrekvens
    abstract val startTidspunkt: LocalTime
    abstract val sluttTidspunkt: LocalTime
    abstract val ukeDag: String
    abstract val startDato: LocalDate
    abstract val sluttDato: LocalDate
    abstract val merknad: String
}