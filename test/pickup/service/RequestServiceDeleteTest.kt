package pickup.service

import arrow.core.Either
import ombruk.backend.calendar.database.Stations
import ombruk.backend.calendar.model.Station
import ombruk.backend.partner.database.Partners
import ombruk.backend.partner.model.Partner
import ombruk.backend.pickup.database.Pickups
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
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import java.time.LocalDateTime
import java.time.LocalTime
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
                    it[name] = "TestPartner 1"
                    it[description] = "Description of TestPartner 1"
                    it[phone] = "+47 2381931"
                    it[email] = "example@gmail.com"
                }.value

                testPartner =
                    Partner(
                        testPartnerId,
                        "TestPartner 1",
                        "Description of TestPartner 1",
                        "+47 2381931",
                        "example@gmail.com"
                    )

                val testPartnerId2 = Partners.insertAndGetId {
                    it[name] = "TestPartner 2"
                    it[description] = "Description of TestPartner 2"
                    it[phone] = "911"
                    it[email] = "example@gmail.com"
                }.value

                testPartner2 = Partner(
                    testPartnerId2,
                    "TestPartner 2",
                    "Description of TestPartner 2",
                    "911",
                    "example@gmail.com"
                )


                val testStationId = Stations.insertAndGetId {
                    it[name] = "Test Station 1"
                    it[openingTime] = "09:00:00"
                    it[closingTime] = "21:00:00"
                }.value

                testStation = Station(
                    testStationId,
                    "Test Station 1",
                    LocalTime.parse("09:00:00", DateTimeFormatter.ISO_TIME),
                    LocalTime.parse("21:00:00", DateTimeFormatter.ISO_TIME)
                )

                val testStationId2 = Stations.insertAndGetId {
                    it[name] = "Test Station 2"
                    it[openingTime] = "08:00:00"
                    it[closingTime] = "20:00:00"
                }.value
                testStation2 = Station(
                    testStationId2,
                    "Test Station 2",
                    LocalTime.parse("08:00:00", DateTimeFormatter.ISO_TIME),
                    LocalTime.parse("20:00:00", DateTimeFormatter.ISO_TIME)
                )
            }

            requestService = RequestService
        }
        @AfterClass
        @JvmStatic
        fun cleanPartnersAndStationsFromDB(){
            transaction {
                Partners.deleteAll()
                Stations.deleteAll()
            }
        }
    }

    @After
    fun cleanEventsFromDB() {
        transaction {
            Requests.deleteAll()
            Pickups.deleteAll()
        }
    }

    @Test
    fun testAddPartnersToPickup() {
        val start = LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val end = LocalDateTime.parse("2020-08-14T16:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val pickup = PickupService.savePickup(CreatePickupForm(start, end, testStation.id))
        require(pickup is Either.Right)
        val expectedRequest = Request(pickup.b.id, testPartner)
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
        require(pickup1 is Either.Right)
        require(pickup2 is Either.Right)

        val requestToDelete = Request(pickup1.b.id, testPartner)
        val requestNotToDelete = Request(pickup2.b.id, testPartner)

        requestService.addPartnersToPickup(requestToDelete)
        requestService.addPartnersToPickup(requestNotToDelete)

        requestService.deleteRequests(pickup1.b.id, null, null)
        val requestsInRepositoryAfterDelete = requestService.getRequests(null, null)

        assert(!requestsInRepositoryAfterDelete.contains(requestToDelete))
        assert(requestsInRepositoryAfterDelete.contains(requestNotToDelete))
    }

    @Test
    fun testDeleteRequestByPartnerID() {
        val start = LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val end = LocalDateTime.parse("2020-08-14T16:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val pickup = PickupService.savePickup(CreatePickupForm(start, end, testStation.id))

        require(pickup is Either.Right)

        val requestToDelete = Request(pickup.b.id, testPartner)
        val requestNotToDelete = Request(pickup.b.id, testPartner2)

        requestService.addPartnersToPickup(requestToDelete)
        requestService.addPartnersToPickup(requestNotToDelete)

        requestService.deleteRequests(null, testPartner.id, null)
        val requestsInRepositoryAfterDelete = requestService.getRequests(null, null)

        assert(!requestsInRepositoryAfterDelete.contains(requestToDelete))
        assert(requestsInRepositoryAfterDelete.contains(requestNotToDelete))
    }

    @Test
    fun testDeleteRequestByStationID() {
        val start = LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val end = LocalDateTime.parse("2020-08-14T16:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val pickup1 = PickupService.savePickup(CreatePickupForm(start, end, testStation.id))
        val pickup2 = PickupService.savePickup(CreatePickupForm(start, end, testStation2.id))

        require(pickup1 is Either.Right)
        require(pickup2 is Either.Right)

        val requestToDelete = Request(pickup1.b.id, testPartner)
        val requestNotToDelete = Request(pickup2.b.id, testPartner)

        requestService.addPartnersToPickup(requestToDelete)
        requestService.addPartnersToPickup(requestNotToDelete)

        requestService.deleteRequests(null, null, testStation.id)
        val requestsInRepositoryAfterDelete = requestService.getRequests(null, null)

        assert(!requestsInRepositoryAfterDelete.contains(requestToDelete))
        assert(requestsInRepositoryAfterDelete.contains(requestNotToDelete))
    }

    @Test
    fun testDeleteRequestByStationIDAndPartnerID() {
        val start = LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val end = LocalDateTime.parse("2020-08-14T16:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val pickup1 = PickupService.savePickup(CreatePickupForm(start, end, testStation.id))
        val pickup2 = PickupService.savePickup(CreatePickupForm(start, end, testStation2.id))

        require(pickup1 is Either.Right)
        require(pickup2 is Either.Right)

        val requestToDelete = Request(pickup1.b.id, testPartner)
        val requestNotToDelete = Request(pickup2.b.id, testPartner2)

        requestService.addPartnersToPickup(requestToDelete)
        requestService.addPartnersToPickup(requestNotToDelete)

        requestService.deleteRequests(null, testPartner.id, testStation.id)
        val requestsInRepositoryAfterDelete = requestService.getRequests(null, null)

        assert(!requestsInRepositoryAfterDelete.contains(requestToDelete))
        assert(requestsInRepositoryAfterDelete.contains(requestNotToDelete))
    }

    @Test
    fun testDeleteRequestAllParams() {
        val start = LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val end = LocalDateTime.parse("2020-08-14T16:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val pickup1 = PickupService.savePickup(CreatePickupForm(start, end, testStation.id))
        val pickup2 = PickupService.savePickup(CreatePickupForm(start, end, testStation2.id))

        require(pickup1 is Either.Right)
        require(pickup2 is Either.Right)

        val requestToDelete = Request(pickup1.b.id, testPartner)
        val requestNotToDelete = Request(pickup2.b.id, testPartner2)

        requestService.addPartnersToPickup(requestToDelete)
        requestService.addPartnersToPickup(requestNotToDelete)

        requestService.deleteRequests(pickup1.b.id, testPartner.id, testStation.id)
        val requestsInRepositoryAfterDelete = requestService.getRequests(null, null)

        assert(!requestsInRepositoryAfterDelete.contains(requestToDelete))
        assert(requestsInRepositoryAfterDelete.contains(requestNotToDelete))
    }

}

