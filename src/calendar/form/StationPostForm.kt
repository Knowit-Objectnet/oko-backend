package ombruk.backend.calendar.form

import kotlinx.serialization.Serializable
import ombruk.backend.shared.model.serializer.LocalTimeSerializer
import java.time.LocalTime

@Serializable
data class StationPostForm(
    val name: String,
    @Serializable(with = LocalTimeSerializer::class) val openingTime: LocalTime,
    @Serializable(with = LocalTimeSerializer::class) val closingTime: LocalTime
)