package ombruk.backend.calendar.model

import kotlinx.serialization.Serializable
import ombruk.backend.shared.model.serializer.LocalTimeSerializer
import java.time.LocalTime

@Serializable
data class Station(
    val id: Int,
    var name: String,
    @Serializable(with = LocalTimeSerializer::class) val openingTime: LocalTime,
    @Serializable(with = LocalTimeSerializer::class) val closingTime: LocalTime
)