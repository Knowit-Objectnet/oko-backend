package ombruk.backend.calendar.form

import kotlinx.serialization.Serializable
import ombruk.backend.shared.model.serializer.LocalDateTimeSerializer
import java.time.LocalDateTime

@Serializable
data class EventUpdateForm(
    val id: Int,
    @Serializable(with = LocalDateTimeSerializer::class) val startDateTime: LocalDateTime ? = null,
    @Serializable(with = LocalDateTimeSerializer::class) val endDateTime: LocalDateTime ? = null
    )