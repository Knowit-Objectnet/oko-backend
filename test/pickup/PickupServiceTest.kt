/*
class PickupServiceTest {
    companion object {
        lateinit var pickupService: PickupService
        val testStation = Station(10, "TestStation 1")
        val testStation2 = Station(11, "TestStation 2")

        fun assertPickupEqual(expected: Pickup, actual: Pickup) {
            assertEquals(expected.id, actual.id)
            assertEquals(expected.startTime, actual.startTime)
            assertEquals(expected.endTime, actual.endTime)
            assertEquals(expected.station, actual.station)
        }


        class LocalDateTimeIterator(
            startDate: LocalDateTime,
            private val endDateInclusive: LocalDateTime,
            private val stepDays: Long
        ) : Iterator<LocalDateTime> {

            private var currentDate = startDate
            override fun hasNext() = currentDate <= endDateInclusive
            override fun next(): LocalDateTime {
                val next = currentDate
                currentDate = currentDate.plusDays(stepDays)
                return next
            }
        }

        class LocalDateTimeProgression(
            override val start: LocalDateTime,
            override val endInclusive: LocalDateTime,
            private val stepDays: Long = 1
        ) :
            Iterable<LocalDateTime>, ClosedRange<LocalDateTime> {

            override fun iterator(): Iterator<LocalDateTime> = LocalDateTimeIterator(start, endInclusive, stepDays)
        }

        operator fun LocalDateTime.rangeTo(other: LocalDateTime) = LocalDateTimeProgression(this, other)

        @BeforeClass
        @JvmStatic
        fun setup() {
            initDB()
            transaction {
                listOf(testStation, testStation2).forEach { station ->
                    Stations.insert {
                        it[name] = station.name
                    }
                }
            }

            pickupService = PickupService
        }
    }

    @After
    fun cleanEventsFromDB() {
        transaction {
            Pickups.deleteAll()

        }
    }

    @Test
    fun testGetPickupById() {
        val i = 1
        val startTime = LocalDateTime.parse("2020-07-20T15:45:00", DateTimeFormatter.ISO_DATE_TIME)
        val endTime = LocalDateTime.parse("2020-07-20T16:45:00", DateTimeFormatter.ISO_DATE_TIME)
        val station = testStation
        val id = transaction {
            pickupService.savePickup(
                Pickup(
                    i, startTime, endTime, station
                )
            )
        }


        val actualPickup = pickupService.getPickupById(id)
        assertNotNull(actualPickup, "actualPickup should not be null here:(") {
            assertEquals(id, it.id)
            assertEquals(startTime, it.startTime)
            assertEquals(endTime, it.endTime)
            assertEquals(station, it.station)
        }
    }


    @Test
    fun testGetAllPickups() {
        val start = LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val end = LocalDateTime.parse("2020-08-14T16:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val dateRange = listOf(start..end).flatten()

        val expectedPickups = dateRange.map { startDate ->
            transaction { 
                pickupService.getPickupById(pickupService.savePickup(
                    Pickup(
                        10,
                        startDate,
                        startDate.plusHours(1),
                        testStation
                    )
                )
                )}
        }


        val actualPickups = pickupService.getPickups(null)

        expectedPickups.forEach { expectedPickup ->
            val actualPickup = actualPickups.find { it.id == expectedPickup!!.id }
            assertNotNull(actualPickup) {
                assertPickupEqual(expectedPickup!!, it)
            }
        }
    }

    @Test
    fun testDeletePickupById(){
        val startTime = LocalDateTime.parse("2020-07-20T15:45:00", DateTimeFormatter.ISO_DATE_TIME)
        val endTime = LocalDateTime.parse("2020-07-20T16:45:00", DateTimeFormatter.ISO_DATE_TIME)
        val id = pickupService.savePickup(
            Pickup(
                11,
                startTime,
                endTime,
                testStation2
            )
        )

        assertTrue(pickupService.deletePickup(id, null))

        assertNull(pickupService.getPickupById(id))
    }

    @Test
    fun testDeletePickupByStation(){
        val startTime = LocalDateTime.parse("2020-07-20T15:45:00", DateTimeFormatter.ISO_DATE_TIME)
        val endTime = LocalDateTime.parse("2020-07-20T16:45:00", DateTimeFormatter.ISO_DATE_TIME)
        pickupService.savePickup(
            Pickup(
                12,
                startTime,
                endTime,
                testStation2
            )
        )

        assertTrue(pickupService.deletePickup(null, testStation2.id))

        assertTrue(pickupService.getPickups(testStation2.id).isEmpty())
    }

    @Test
    fun testUpdatePickup(){
        val startTime = LocalDateTime.parse("2020-07-20T15:45:00", DateTimeFormatter.ISO_DATE_TIME)
        val endTime = LocalDateTime.parse("2020-07-20T16:45:00", DateTimeFormatter.ISO_DATE_TIME)
        val pickup = Pickup(13, startTime, endTime, testStation)
        val id = pickupService.savePickup(pickup)

        val updatedPickup = Pickup(id, startTime, endTime, testStation2)

        assertTrue(pickupService.updatePickup(updatedPickup))
        val res = pickupService.getPickupById(id)
        val result = pickupService.getPickups(null)
        assertTrue(result.contains(res))
        assertTrue(result.contains(updatedPickup))
        assertFalse(result.contains(pickup))
        assertEquals(updatedPickup, res)
    }

    @Test
    fun testUpdatePickupNotValidInput(){
        val startTime = LocalDateTime.parse("2020-07-20T15:45:00", DateTimeFormatter.ISO_DATE_TIME)
        val endTime = LocalDateTime.parse("2020-07-20T16:45:00", DateTimeFormatter.ISO_DATE_TIME)
        val pickup = Pickup(14, startTime, endTime, testStation)
        val id = pickupService.savePickup(pickup)
        val expectedPickup = pickup.copy(id=id)

        val updatedPickup = Pickup(78, startTime, endTime, testStation2)

        assertFalse(pickupService.updatePickup(updatedPickup))
        val actualPickup = pickupService.getPickupById(id)
        val resultList = pickupService.getPickups(null)
        assertTrue(resultList.contains(actualPickup))
        assertFalse(resultList.contains(updatedPickup))
        assertTrue(resultList.contains(expectedPickup))
        assertEquals(expectedPickup, actualPickup)
    }
}
*/
