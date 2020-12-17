package no.oslokommune.ombruk.stasjon.model

import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.Serializable
import no.oslokommune.ombruk.shared.model.serializer.LocalTimeSerializer
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

// description = "An array of days the Stasjon is open. The first array value is the opening time, and the last is the closing time",
@Serializable
data class Stasjon(
    @field:Schema(type = "object", description = "The ID of the Stasjon", nullable = false) val id: Int,
    @field:Schema(description = "The name of the Stasjon", nullable = false, example = "Haraldrud") var navn: String,
    @field:Schema(
        description = "Describes the days in which the Stasjon is open. " +
                "If a day is not present, the Stasjon is closed. " +
                " The first value of the array denotes the opening time, and the second the closing time",
        example = "{\n" +
            "      \"MONDAY\": [\n" +
                    "        \"09:00:00Z\",\n" +
                    "        \"20:00:00Z\"\n" +
                    "      ],\n" +
                    "      \"TUESDAY\": [\n" +
                    "        \"08:00:00Z\",\n" +
                    "        \"21:00:00Z\"\n" +
                    "      ],\n" +
                    "      \"WEDNESDAY\": [\n" +
                    "        \"09:00:00Z\",\n" +
                    "        \"20:00:00Z\"\n" +
                    "      ],\n" +
                    "      \"THURSDAY\": [\n" +
                    "        \"09:00:00Z\",\n" +
                    "        \"20:00:00Z\"\n" +
                    "      ],\n" +
                    "      \"FRIDAY\": [\n" +
                    "        \"09:00:00Z\",\n" +
                    "        \"20:00:00Z\"\n" +
                    "      ]\n" +
                    "    }\n"
    ) val aapningstider: Map<DayOfWeek, List<@Serializable(with = LocalTimeSerializer::class) LocalTime>>? = null
)