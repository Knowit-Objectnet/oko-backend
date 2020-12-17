package no.oslokommune.ombruk.uttak.model

import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.Serializable
import no.oslokommune.ombruk.shared.model.serializer.LocalDateTimeSerializer
import java.time.DayOfWeek
import java.time.LocalDateTime

@Serializable
data class GjentakelsesRegel(
    @field:Schema(description = "ID of the GjentakelsesRegel. Used to connect Uttak in a series") var id: Int? = null, // why is this nullable?
//    @field:Schema(
//        description = "The date of which the rule was last changed. If null, the rule never changed",
//        nullable = true
//    ) @Serializable(with = LocalDateTimeSerializer::class) val endretTidspunkt: LocalDateTime? = null,
    @field:Schema(
        description = "The date of when the GjentakelsesRegel ends."
    ) @Serializable(with = LocalDateTimeSerializer::class) var until: LocalDateTime? = null,
//    @field:Schema(
//        description = "The date at which the GjentakelsesRegel and its corresponding Uttak were deleted. If null, it has not been deleted"
//    ) @Serializable(with = LocalDateTimeSerializer::class) val slettetTidspunkt: LocalDateTime? = null,
    @field:Schema(
        description = "A list of days in which the GjentakelsesRegel is applicable. " +
                "An Uttak that occurs every Monday and Friday would have [\"MONDAY\", \"FRIDAY\"] as its value"
    ) @Serializable val dager: List<DayOfWeek>,
    @field:Schema(
        description = "Describes how often the GjentakelsesRegel should take place in terms of a weekly count. " +
                "If a value of 2 is passed, the rule will apply every other week",
        defaultValue = "1"
    ) val intervall: Int = 1,
    @field:Schema(
        description = "Describes the amount of times a GjentakelsesRegel should be applied. Generates X amount of Uttak.",
        nullable = true
    ) var antall: Int? = null
)

fun String.toWeekDayList() = this.split(", ").map { DayOfWeek.valueOf(it) }