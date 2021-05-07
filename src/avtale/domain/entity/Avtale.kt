package ombruk.backend.avtale.domain.entity

import kotlinx.serialization.Serializable
import ombruk.backend.aktor.domain.entity.Aktor
import ombruk.backend.avtale.model.AvtaleType
import ombruk.backend.henting.domain.entity.Henteplan
import ombruk.backend.shared.model.serializer.LocalDateSerializer
import shared.model.serializer.UUIDSerializer
import java.time.LocalDate
import java.util.*

@Serializable(with = LocalDateSerializer::class)
data class Avtale(
    val id: UUID,
    val aktorId: UUID,
    val type: AvtaleType,
    val startDato: LocalDate,
    val sluttDato: LocalDate,
    val henteplaner: List<Henteplan> = emptyList()
)
