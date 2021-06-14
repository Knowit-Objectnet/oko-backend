package ombruk.backend.avtale.domain.params

import kotlinx.serialization.Serializable
import ombruk.backend.avtale.model.AvtaleType
import ombruk.backend.core.domain.model.UpdateParams
import shared.model.serializer.UUIDSerializer
import java.time.LocalDate
import java.util.*

@Serializable
abstract class AvtaleUpdateParams: UpdateParams{
    abstract override val id: UUID
    abstract val type: AvtaleType?
    abstract val startDato: LocalDate
    abstract val sluttDato: LocalDate
//    abstract val henteplaner: List<HenteplanUpdateParams>
}