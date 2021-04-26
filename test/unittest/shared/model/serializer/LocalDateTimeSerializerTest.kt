package shared.model.serializer

import io.ktor.serialization.DefaultJsonConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import ombruk.backend.shared.model.serializer.LocalDateTimeSerializer
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDateTime
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LocalDateTimeSerializerTest {

    @Serializable
    data class LocalDateTimeContainer(
        @Serializable(with = LocalDateTimeSerializer::class) val dt: LocalDateTime
    )

    @Suppress("unused")
    fun parameterProvider() = listOf(
        LocalDateTime.parse("2020-07-06T15:48:06") to "2020-07-06T15:48:06Z",
        LocalDateTime.parse("2028-11-24T00:00:00") to "2028-11-24T00:00:00Z",
        LocalDateTime.parse("1772-11-24T10:10:10") to "1772-11-24T10:10:10Z",
        LocalDateTime.parse("2020-12-31T23:59:59") to "2020-12-31T23:59:59Z",
        LocalDateTime.parse("2020-01-01T00:00:00") to "2020-01-01T00:00:00Z",
        LocalDateTime.parse("3020-07-06T15:48:06") to "3020-07-06T15:48:06Z",
        LocalDateTime.parse("0000-07-06T15:48:06") to "0000-07-06T15:48:06Z"
    )

    @ParameterizedTest
    @MethodSource("parameterProvider")
    fun `serialize local date time`(dtToString: Pair<LocalDateTime, String>) {
        val expected = "\"${dtToString.second}\""
        val actual = Json.encodeToString(LocalDateTimeSerializer, dtToString.first)
        assertEquals(expected, actual)
    }


    @ParameterizedTest
    @MethodSource("parameterProvider")
    fun `deserialize local date time`(dtToString: Pair<LocalDateTime, String>) {
        val expected = dtToString.first
        val actual = Json.decodeFromString(
            LocalDateTimeContainer.serializer(),
            "{\"dt\": \"${dtToString.second}\"}"
        )
        assertEquals(expected, actual.dt)
    }

}