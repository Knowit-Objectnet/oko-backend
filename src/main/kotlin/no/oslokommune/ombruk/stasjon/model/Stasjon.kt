package no.oslokommune.ombruk.stasjon.model

import kotlinx.serialization.Serializable
import no.oslokommune.ombruk.shared.model.serializer.LocalTimeSerializer
import java.time.DayOfWeek
import java.time.LocalTime

@Serializable
data class Stasjon(
    val id: Int,
    var navn: String,
    val aapningstider: Map<DayOfWeek, List<@Serializable(with = LocalTimeSerializer::class) LocalTime>>? = null
)