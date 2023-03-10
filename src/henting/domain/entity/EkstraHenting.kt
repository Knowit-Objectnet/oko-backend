package ombruk.backend.henting.domain.entity

import kotlinx.serialization.Serializable
import ombruk.backend.kategori.domain.entity.EkstraHentingKategori
import ombruk.backend.shared.model.serializer.LocalDateTimeSerializer
import ombruk.backend.utlysning.domain.entity.Utlysning
import ombruk.backend.vektregistrering.domain.entity.Vektregistrering
import shared.model.serializer.UUIDSerializer
import java.time.LocalDateTime
import java.util.*

@Serializable
data class EkstraHenting(
    @Serializable(with = UUIDSerializer::class) override val id: UUID,
    @Serializable(with = LocalDateTimeSerializer::class) override val startTidspunkt: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class) override val sluttTidspunkt: LocalDateTime,
    val beskrivelse: String,
    @Serializable(with = UUIDSerializer::class) val stasjonId: UUID,
    val stasjonNavn: String,
    val godkjentUtlysning: Utlysning? = null,
    val kategorier: List<EkstraHentingKategori> = emptyList(),
    val utlysninger: List<Utlysning> = emptyList(),
    override val vektregistreringer: List<Vektregistrering>? = emptyList()
) : Henting()