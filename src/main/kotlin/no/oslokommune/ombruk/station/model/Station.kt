package no.oslokommune.ombruk.station.model

import kotlinx.serialization.Serializable
import no.oslokommune.ombruk.shared.model.serializer.LocalTimeSerializer
import java.time.DayOfWeek
import java.time.LocalTime

@Serializable
data class Station(
    val id: Int,
    var name: String,
    val hours: Map<DayOfWeek, List<@Serializable(with = LocalTimeSerializer::class) LocalTime>>? = null
)