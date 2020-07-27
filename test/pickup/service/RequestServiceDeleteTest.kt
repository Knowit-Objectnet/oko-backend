package pickup.service

import ombruk.backend.calendar.database.Stations
import ombruk.backend.calendar.model.Station
import ombruk.backend.partner.database.Partners
import ombruk.backend.partner.model.Partner
import ombruk.backend.pickup.database.Requests
import ombruk.backend.pickup.form.CreatePickupForm
import ombruk.backend.pickup.model.Request
import ombruk.backend.pickup.service.PickupService
import ombruk.backend.pickup.service.RequestService
import ombruk.backend.shared.database.initDB
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.After
import org.junit.BeforeClass
import org.junit.Test
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals


class RequestServiceTest {
    companion object {
        lateinit var requestService: RequestService
        lateinit var testPartner: Partner
        lateinit var testPartner2: Partner
        lateinit var testStation: Station
        lateinit var testStation2: Station

        @BeforeClass
        @JvmStatic
        fun setup() {
            initDB()
            transaction {
                val testPartnerId = Partners.insertAndGetId {
                    it[name] = "Test Partner 1"
                }.value

                testPartner = Partner(testPartnerId, "Test Partner 1")

                val testPartnerId2 = Partners.insertAndGetId {
                    it[name] = "Test Partner 2"
                }.value

                testPartner2 = Partner(testPartnerId2, "Test Partner 2")


                val testStationId = Stations.insertAndGetId {
                    it[name] = "Test Station 1"
                }.value

                testStation = Station(testStationId, "Test Station 1")

                val testStationId2 = Stations.insertAndGetId {
                    it[name] = "Test Station 2"
                }.value
                testStation2 = Station(testStationId2, "Test Station 2")
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
    fun testAddPartnersToPickup() {
        val start = LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val end = LocalDateTime.parse("2020-08-14T16:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val pickup = PickupService.savePickup(CreatePickupForm(start, end, testStation.id))

        val expectedRequest = Request(pickup.id, testPartner)
        requestService.addPartnersToPickup(expectedRequest)

        val actualRequests = requestService.getRequests(expectedRequest.pickupID, null)
        assertEquals(expectedRequest, actualRequests.first())
    }

    @Test
    fun testDeleteRequestByPickupID() {
        val start = LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val end = LocalDateTime.parse("2020-08-14T16:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val pickup1 = PickupService.savePickup(CreatePickupForm(start, end, testStation.id))
        val pickup2 = PickupService.savePickup(CreatePickupForm(start, end, testStation.id))


        val requestToDelete = Request(pickup1.id, testPartner)
        val requestNotToDelete = Request(pickup2.id, testPartner)

        requestService.addPartnersToPickup(requestToDelete)
        requestService.addPartnersToPickup(requestNotToDelete)

        requestService.deleteRequests(pickup1.id, null, null)
        val requestsInRepositoryAfterDelete = requestService.getRequests(null, null)

        assert(!requestsInRepositoryAfterDelete.contains(requestToDelete))
        assert(requestsInRepositoryAfterDelete.contains(requestNotToDelete))
    }

    @Test
    fun testDeleteRequestByPartnerID(){
        val start = LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val end = LocalDateTime.parse("2020-08-14T16:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val pickup = PickupService.savePickup(CreatePickupForm(start, end, testStation.id))


        val requestToDelete = Request(pickup.id, testPartner)
        val requestNotToDelete = Request(pickup.id, testPartner2)

        requestService.addPartnersToPickup(requestToDelete)
        requestService.addPartnersToPickup(requestNotToDelete)

        requestService.deleteRequests(null, testPartner.id, null)
        val requestsInRepositoryAfterDelete = requestService.getRequests(null, null)

        assert(!requestsInRepositoryAfterDelete.contains(requestToDelete))
        assert(requestsInRepositoryAfterDelete.contains(requestNotToDelete))
    }

    @Test
    fun testDeleteRequestByStationID(){
        val start = LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val end = LocalDateTime.parse("2020-08-14T16:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val pickup1 = PickupService.savePickup(CreatePickupForm(start, end, testStation.id))
        val pickup2 = PickupService.savePickup(CreatePickupForm(start, end, testStation2.id))


        val requestToDelete = Request(pickup1.id, testPartner)
        val requestNotToDelete = Request(pickup2.id, testPartner)

        requestService.addPartnersToPickup(requestToDelete)
        requestService.addPartnersToPickup(requestNotToDelete)

        requestService.deleteRequests(null, null, testStation.id)
        val requestsInRepositoryAfterDelete = requestService.getRequests(null, null)

        assert(!requestsInRepositoryAfterDelete.contains(requestToDelete))
        assert(requestsInRepositoryAfterDelete.contains(requestNotToDelete))
    }

    @Test
    fun testDeleteRequestByStationIDAndPartnerID(){
        val start = LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val end = LocalDateTime.parse("2020-08-14T16:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val pickup1 = PickupService.savePickup(CreatePickupForm(start, end, testStation.id))
        val pickup2 = PickupService.savePickup(CreatePickupForm(start, end, testStation2.id))


        val requestToDelete = Request(pickup1.id, testPartner)
        val requestNotToDelete = Request(pickup2.id, testPartner2)

        requestService.addPartnersToPickup(requestToDelete)
        requestService.addPartnersToPickup(requestNotToDelete)

        requestService.deleteRequests(null, testPartner.id, testStation.id)
        val requestsInRepositoryAfterDelete = requestService.getRequests(null, null)

        assert(!requestsInRepositoryAfterDelete.contains(requestToDelete))
        assert(requestsInRepositoryAfterDelete.contains(requestNotToDelete))
    }

    @Test
    fun testDeleteRequestAllParams(){
        val start = LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val end = LocalDateTime.parse("2020-08-14T16:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val pickup1 = PickupService.savePickup(CreatePickupForm(start, end, testStation.id))
        val pickup2 = PickupService.savePickup(CreatePickupForm(start, end, testStation2.id))


        val requestToDelete = Request(pickup1.id, testPartner)
        val requestNotToDelete = Request(pickup2.id, testPartner2)

        requestService.addPartnersToPickup(requestToDelete)
        requestService.addPartnersToPickup(requestNotToDelete)

        requestService.deleteRequests(pickup1.id, testPartner.id, testStation.id)
        val requestsInRepositoryAfterDelete = requestService.getRequests(null, null)

        assert(!requestsInRepositoryAfterDelete.contains(requestToDelete))
        assert(requestsInRepositoryAfterDelete.contains(requestNotToDelete))
    }

}

