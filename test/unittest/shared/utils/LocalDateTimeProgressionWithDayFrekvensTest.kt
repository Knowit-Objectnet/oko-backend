package shared.utils

import ombruk.backend.henting.domain.model.HenteplanFrekvens
import ombruk.backend.shared.utils.LocalDateTimeProgressionWithDayFrekvens
import org.junit.jupiter.api.Test

import java.time.DayOfWeek
import java.time.LocalDateTime

internal class LocalDateTimeProgressionWithDayFrekvensTest {

    @Test
    fun testConstructor() {
        val myProgression1 = LocalDateTimeProgressionWithDayFrekvens(
            LocalDateTime.of(2021, 5,13,10,0),
            LocalDateTime.of(2021, 5,30,10,0),
            DayOfWeek.FRIDAY,
            HenteplanFrekvens.UKENTLIG
        )

        val myList1 = myProgression1.map { it }
        assert(myList1.size == 3)
        assert(myList1.containsAll(listOf(
            LocalDateTime.of(2021, 5,14,10,0),
            LocalDateTime.of(2021, 5,21,10,0),
            LocalDateTime.of(2021, 5,28,10,0)
        )))

        val myProgression2 = LocalDateTimeProgressionWithDayFrekvens(
            LocalDateTime.of(2021, 5,13,10,0),
            LocalDateTime.of(2021, 5,30,10,0),
            DayOfWeek.MONDAY,
            HenteplanFrekvens.UKENTLIG
        )

        val myList2 = myProgression2.map { it }
        assert(myList2.size == 2)
        assert(myList2.containsAll(listOf(
            LocalDateTime.of(2021, 5,17,10,0),
            LocalDateTime.of(2021, 5,24,10,0)
        )))

        val myProgression3 = LocalDateTimeProgressionWithDayFrekvens(
            LocalDateTime.of(2021, 1,1,10,0),
            LocalDateTime.of(2022, 1,1,10,0),
            DayOfWeek.FRIDAY,
            HenteplanFrekvens.UKENTLIG
        )

        val myList3 = myProgression3.map { it }
        assert(myList3.size == 53)
        assert(myList3.contains(
            LocalDateTime.of(2021, 1,1,10,0)
        ))

        val myProgression4 = LocalDateTimeProgressionWithDayFrekvens(
            LocalDateTime.of(2021, 5,13,10,0),
            LocalDateTime.of(2021, 6,30,10,0),
            DayOfWeek.FRIDAY,
            HenteplanFrekvens.ANNENHVER
        )

        val myList4 = myProgression4.map { it }
        assert(myList4.size == 4)
        assert(myList4.containsAll(listOf(
            LocalDateTime.of(2021, 5,14,10,0),
            LocalDateTime.of(2021, 5,28,10,0),
            LocalDateTime.of(2021, 6,11,10,0),
            LocalDateTime.of(2021, 6,25,10,0),
        )))

        val myProgression5 = LocalDateTimeProgressionWithDayFrekvens(
            LocalDateTime.of(2021, 1,1,10,0),
            LocalDateTime.of(2022, 1,1,10,0),
            DayOfWeek.FRIDAY,
            HenteplanFrekvens.ANNENHVER
        )

        val myList5 = myProgression5.map { it }
        assert(myList5.size == 27)
        assert(myList5.contains(
            LocalDateTime.of(2021, 1,1,10,0)
        ))

        val myProgression6 = LocalDateTimeProgressionWithDayFrekvens(
            LocalDateTime.of(2021, 1,1,10,0),
            LocalDateTime.of(2022, 1,1,10,0),
            DayOfWeek.FRIDAY,
            HenteplanFrekvens.ENKELT
        )

        val myList6 = myProgression6.map { it }
        assert(myList6.size == 1)
        assert(myList6.contains(
            LocalDateTime.of(2021, 1,1,10,0)
        ))

    }
}