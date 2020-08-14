package shared.model.serializer

import io.ktor.serialization.DefaultJsonConfiguration
import kotlinx.serialization.json.Json
import ombruk.backend.shared.model.serializer.DayOfWeekSerializer
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.time.DayOfWeek
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DayOfWeekSerializerTest {

    @Suppress("unused")
    fun daysParameterProvider() = listOf(
        DayOfWeek.MONDAY to "MONDAY",
        DayOfWeek.TUESDAY to "TUESDAY",
        DayOfWeek.WEDNESDAY to "WEDNESDAY",
        DayOfWeek.THURSDAY to "THURSDAY",
        DayOfWeek.FRIDAY to "FRIDAY",
        DayOfWeek.SATURDAY to "SATURDAY",
        DayOfWeek.SUNDAY to "SUNDAY"
    )

    @ParameterizedTest
    @MethodSource("daysParameterProvider")
    fun `serialize days`(dayToString: Pair<DayOfWeek, String>) {
        val expected = "\"${dayToString.second}\""
        val actual = Json(DefaultJsonConfiguration).stringify(DayOfWeekSerializer, dayToString.first)
        assertEquals(expected, actual)
    }


    @ParameterizedTest
    @MethodSource("daysParameterProvider")
    fun `deserialize days`(dayToString: Pair<DayOfWeek, String>) {
        val expected = dayToString.first
        val actual = Json(DefaultJsonConfiguration).parse(DayOfWeekSerializer, dayToString.second)
        assertEquals(expected, actual)
    }

}