package no.oslokommune.ombruk.uttak.model

import kotlinx.serialization.Serializable
import no.oslokommune.ombruk.shared.model.serializer.LocalDateTimeSerializer
import java.time.DayOfWeek
import java.time.LocalDateTime

@Serializable
data class GjentakelsesRegel(
    var id: Int? = null,
    @Serializable(with = LocalDateTimeSerializer::class) val endretTidspunkt: LocalDateTime? = null,
    @Serializable(with = LocalDateTimeSerializer::class) var sluttTidspunkt: LocalDateTime? = null,
    @Serializable(with = LocalDateTimeSerializer::class) val slettetTidspunkt: LocalDateTime? = null,
    @Serializable val dager: List<DayOfWeek>,
    val intervall: Int = 1,
    var antall: Int? = null
)

fun String.toWeekDayList() = this.split(", ").map { DayOfWeek.valueOf(it) }