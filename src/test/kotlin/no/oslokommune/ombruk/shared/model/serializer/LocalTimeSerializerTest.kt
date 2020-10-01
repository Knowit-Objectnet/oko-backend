package no.oslokommune.ombruk.shared.model.serializer

import io.ktor.serialization.DefaultJsonConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import no.oslokommune.ombruk.shared.model.serializer.LocalTimeSerializer
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalTime
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LocalTimeSerializerTest {

    @Serializable
    data class LocalTimeContainer(
        @Serializable(with = LocalTimeSerializer::class) val t: LocalTime
    )

    @Suppress("unused")
    fun parameterProvider() = listOf(
        LocalTime.parse("15:48:06") to "15:48:06Z",
        LocalTime.parse("00:00:00") to "00:00:00Z",
        LocalTime.parse("10:10:10") to "10:10:10Z",
        LocalTime.parse("23:59:59") to "23:59:59Z"
    )

    @ParameterizedTest
    @MethodSource("parameterProvider")
    fun `serialize local time`(timeToString: Pair<LocalTime, String>) {
        val expected = "\"${timeToString.second}\""
        val actual = Json(DefaultJsonConfiguration).stringify(LocalTimeSerializer, timeToString.first)
        assertEquals(expected, actual)
    }


    @ParameterizedTest
    @MethodSource("parameterProvider")
    fun `deserialize local time`(timeToString: Pair<LocalTime, String>) {
        val expected = timeToString.first
        val actual =
            Json(DefaultJsonConfiguration).parse(LocalTimeContainer.serializer(), "{\"t\": \"${timeToString.second}\"}")
        assertEquals(expected, actual.t)
    }

}