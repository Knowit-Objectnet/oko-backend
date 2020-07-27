/*
class RequestServiceTest {
    companion object {
        lateinit var requestService: RequestService
        val testPartner = Partner(20, "TestPartner 1")
        val testPartner2 = Partner(21, "TestPartner 2")
        val testStation = Station(35, "TestStation 1")
        val testStation2 = Station(36, "TestStation 2")

        val startTime = LocalDateTime.parse("2020-07-20T08:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val endTime = LocalDateTime.parse("2020-07-20T10:00:00", DateTimeFormatter.ISO_DATE_TIME)
        val startTime2 = LocalDateTime.parse("2020-07-20T15:45:00", DateTimeFormatter.ISO_DATE_TIME)
        val endTime2 = LocalDateTime.parse("2020-07-20T16:45:00", DateTimeFormatter.ISO_DATE_TIME)

        @BeforeClass
        @JvmStatic
        fun setup() {
            initDB()
            transaction {
                listOf(testPartner, testPartner2).forEach { partner ->
                    Partners.insert {
                        it[name] = partner.name
                    }
                }
                listOf(testStation, testStation2).forEach { station ->
                    Stations.insert {
                        it[name] = station.name
                    }
                }
            }

            requestService = RequestService
        }
    }

    @After
    fun cleanEventsFromDB() {
        transaction {
            Requests.deleteAll()
        }
    }

    @Test
    fun testAddPartnersToPickup(){
        val pickup = Pickup(20, startTime, endTime, testStation)
        val id = PickupService.savePickup(pickup)

        val request = Request(id, testPartner)

        requestService.addPartnersToPickup(request)

        val res = requestService.getRequests(request.pickupID, null)
        assertFalse(res.isEmpty())
        assertEquals(1, res.size)
        assertTrue(res.contains(request))
    }

    @Test
    fun testDeleteRequestByPickupID(){
        val testPickup = Pickup(21, startTime, endTime, testStation)
        val id = PickupService.savePickup(testPickup)
        val testPickup2 = Pickup(22, startTime2, endTime2, testStation2)
        val id2 = PickupService.savePickup(testPickup2)

        val request = Request(id, testPartner)
        val request2 = Request(id, testPartner2)
        val request3 = Request(id2, testPartner)

        requestService.addPartnersToPickup(request)
        requestService.addPartnersToPickup(request2)
        requestService.addPartnersToPickup(request3)
        assertTrue(requestService.getRequests(null, null).contains(request))

        assertTrue(requestService.deleteRequests(request.pickupID, null, null))
        val result = requestService.getRequests(null, null)
        assertFalse(result.contains(request))
        assertFalse(result.contains(request2))
        assertTrue(result.contains(request3))
    }

    @Test
    fun testDeleteRequestByPartnerID(){
        val testPickup = Pickup(23, startTime, endTime, testStation)
        val id = PickupService.savePickup(testPickup)
        val testPickup2 = Pickup(24, startTime2, endTime2, testStation2)
        val id2 = PickupService.savePickup(testPickup2)

        val request = Request(id, testPartner)
        val request2 = Request(id, testPartner2)
        val request3 = Request(id2, testPartner)

        requestService.addPartnersToPickup(request)
        requestService.addPartnersToPickup(request2)
        requestService.addPartnersToPickup(request3)

        assertTrue(requestService.getRequests(null, null).contains(request))

        assertTrue(requestService.deleteRequests(null, testPartner.id, null))

        val resultList = requestService.getRequests(null, null)
        assertFalse(resultList.contains(request))
        assertTrue(resultList.contains(request2))
        assertFalse(resultList.contains(request3))
    }

    @Test
    fun testDeleteRequestByStationID(){
        val testPickup = Pickup(25, startTime, endTime, testStation)
        val id = PickupService.savePickup(testPickup)
        val testPickup2 = Pickup(26, startTime2, endTime2, testStation2)
        val id2 = PickupService.savePickup(testPickup2)

        val request = Request(id, testPartner)
        val request2 = Request(id, testPartner2)
        val request3 = Request(id2, testPartner)

        requestService.addPartnersToPickup(request)
        requestService.addPartnersToPickup(request2)
        requestService.addPartnersToPickup(request3)

        assertTrue(requestService.getRequests(null, null).contains(request))

        assertTrue(requestService.deleteRequests(null, null, testStation.id))

        val result = requestService.getRequests(null, null)
        assertFalse(result.contains(request))
        assertFalse(result.contains(request2))
        assertTrue(result.contains(request3))
    }

    @Test
    fun testDeleteRequestByStationIDAndPartnerID(){
        val testPickup = Pickup(27, startTime, endTime, testStation)
        val id = PickupService.savePickup(testPickup)
        val testPickup2 = Pickup(28, startTime2, endTime2, testStation2)
        val id2 = PickupService.savePickup(testPickup2)
        val request = Request(id, testPartner)
        val request2 = Request(id, testPartner2)
        val request3 = Request(id2, testPartner)

        requestService.addPartnersToPickup(request)
        requestService.addPartnersToPickup(request2)
        requestService.addPartnersToPickup(request3)

        assertTrue(requestService.getRequests(null, null).contains(request))

        assertTrue(requestService.deleteRequests(null, testPartner.id, testStation.id))

        val result = requestService.getRequests(null, null)
        assertFalse(result.contains(request))
        assertTrue(result.contains(request2))
        assertTrue(result.contains(request3))
    }

    @Test
    fun testDeleteRequestAllParams(){
        val testPickup = Pickup(29, startTime, endTime, testStation)
        val id = PickupService.savePickup(testPickup)
        val testPickup2 = Pickup(30, startTime2, endTime2, testStation2)
        val id2 = PickupService.savePickup(testPickup2)
        val request = Request(id, testPartner)
        val request2 = Request(id, testPartner2)
        val request3 = Request(id2, testPartner)

        requestService.addPartnersToPickup(request)
        requestService.addPartnersToPickup(request2)
        requestService.addPartnersToPickup(request3)

        assertTrue(requestService.getRequests(null, null).contains(request))

        assertFalse(requestService.deleteRequests(id2, testPartner.id, testStation.id))

        val result = requestService.getRequests(null, null)
        assertTrue(result.contains(request))
        assertTrue(result.contains(request2))
        assertTrue(result.contains(request3))
    }

    @Test
    fun testDeleteRequestNoParams(){
        val testPickup = Pickup(31, startTime, endTime, testStation)
        val id = PickupService.savePickup(testPickup)
        val testPickup2 = Pickup(32, startTime2, endTime2, testStation2)
        val id2 = PickupService.savePickup(testPickup2)

        val request = Request(id, testPartner)
        val request2 = Request(id, testPartner2)
        val request3 = Request(id2, testPartner)

        requestService.addPartnersToPickup(request)
        requestService.addPartnersToPickup(request2)
        requestService.addPartnersToPickup(request3)

        assertTrue(requestService.getRequests(null, null).contains(request))

        try{
            requestService.deleteRequests(null, null, null)
        } catch(e: Exception) {
            assertTrue(e is IllegalArgumentException)
            assertEquals("Must set a parameter", e.message)
            val result = requestService.getRequests(null, null)
            assertEquals(3, result.size)
            assertTrue(result.contains(request))
            assertTrue(result.contains(request2))
            assertTrue(result.contains(request3))
        }
    }
}

 */