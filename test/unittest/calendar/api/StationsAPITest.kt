package calendar.api

import arrow.core.left
import arrow.core.right
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.DefaultJsonConfiguration
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.Json
import kotlinx.serialization.parse
import ombruk.backend.calendar.database.StationRepository
import ombruk.backend.calendar.form.event.EventPostForm
import ombruk.backend.calendar.form.station.StationDeleteForm
import ombruk.backend.calendar.form.station.StationGetForm
import ombruk.backend.calendar.form.station.StationPostForm
import ombruk.backend.calendar.form.station.StationUpdateForm
import ombruk.backend.calendar.model.Event
import ombruk.backend.calendar.model.Station
import ombruk.backend.calendar.service.EventService
import ombruk.backend.calendar.service.StationService
import ombruk.backend.partner.model.Partner
import ombruk.backend.shared.api.Authorization
import ombruk.backend.shared.api.JwtMockConfig
import ombruk.backend.shared.database.initDB
import ombruk.backend.shared.error.RepositoryError
import ombruk.backend.shared.error.ServiceError
import ombruk.backend.shared.error.ValidationError
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import testutils.testDelete
import testutils.testGet
import testutils.testPatch
import testutils.testPost
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockKExtension::class)
class StationsAPITest {
    val json = Json(DefaultJsonConfiguration.copy(prettyPrint = true))

    @BeforeEach
    fun setup() {
        mockkObject(StationRepository)
        mockkObject(StationService)
        mockkObject(Authorization)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    @AfterAll
    fun finish(){
        unmockkAll()
    }

    @Nested
    inner class GetById {

        /*
        Check for 200 given a valid ID
         */
        @Test
        fun `get single event 200`() {
            val expected = Station(1, "test")
            every { StationService.getStationById(1) } returns expected.right()

            testGet("/stations/1") {
                val receivedStation: Station = json.parse(Station.serializer(), response.content!!)
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(expected.name, receivedStation.name)
            }
        }

        /*
        Getting a nonexisting event should return a 404
         */
        @Test
        fun `get nonexisting event 404`() {
            every { StationService.getStationById(1) } returns RepositoryError.NoRowsFound("test").left()

            testGet("/stations/1") {
                assertEquals(HttpStatusCode.NotFound, response.status())
                assertEquals("No rows found with provided ID, test", response.content)
            }
        }

        /*
        Id cannot be lower than 0, should return 422.
         */
        @Test
        fun `get with unacceptable input 422`() {
            testGet("/stations/0") {
                assertEquals(HttpStatusCode.UnprocessableEntity, response.status())
                assertEquals("id: Must be greater than 0", response.content)
            }
        }

        /*
        Test for 400 when input can't be parsed to int
         */
        @Test
        fun `get with unprocessable input 400`() {
            testGet("/stations/NaN") {
                assertEquals(HttpStatusCode.BadRequest, response.status())
                assertEquals("id could not be parsed.", response.content)
            }
        }

        /*
        Check for 500 when we encounter a serious error
         */
        @Test
        fun `get single station 500`() {
            every { StationService.getStationById(1) } returns ServiceError("test").left()

            testGet("/stations/1") {
                assertEquals(HttpStatusCode.InternalServerError, response.status())
                assertEquals("test", response.content)
            }
        }
    }

    @Nested
    inner class Get {

        /*
        Get all stations
         */
        @Test
        fun `get stations 200`() {
            val s = Station(1, "Test 1")
            val s2 = Station(2, "Test 2")
            val s3 = Station(3, "Test 3")
            val expected = listOf(s, s2, s3)

            every { StationService.getStations(StationGetForm()) } returns expected.right()

            testGet("/stations/") {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Station.serializer().list, expected), response.content)
            }
        }

        /*
        The name parameter should ensure that returned stations match the parameter value.
         */
        @Test
        fun `get station by name`() {
            val s = Station(1, "Test 1")
            val s2 = Station(2, "Test 2")
            val s3 = Station(3, "Test 3")
            val expected = listOf(s2)

            every { StationService.getStations(StationGetForm("Test 2")) } returns expected.right()

            testGet("/stations/?name=Test+2") {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Station.serializer().list, expected), response.content)
            }
        }

        /*
        A name parameter value that matches no stations should return an empty list
         */
        @Test
        fun `get station by name empty array`() {
            val s = Station(1, "Test 1")
            val s2 = Station(2, "Test 2")
            val s3 = Station(3, "Test 3")
            val expected = emptyList<Station>()

            every { StationService.getStations(StationGetForm("Test")) } returns expected.right()

            testGet("/stations/?name=Test") {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("[\n]", response.content)
            }
        }

        /*
        Check for 500 when we encounter a serious error
         */
        @Test
        fun `get stations 500`() {
            every { StationService.getStations(StationGetForm()) } returns ServiceError("test").left()

            testGet("/stations/") {
                assertEquals(HttpStatusCode.InternalServerError, response.status())
                assertEquals("test", response.content)
            }
        }
    }

    @Nested
    inner class Post {

        /*
        A valid post should return 200
         */
        @Test
        fun `post station only name 200`() {
            val expected = Station(1, "Haraldrud")
            val form = StationPostForm("Haraldrud")

            every { StationRepository.exists(1) } returns false
            every { StationService.saveStation(form) } returns expected.right()

            testPost("/stations/", json.stringify(StationPostForm.serializer(), form)) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Station.serializer(), expected), response.content)
            }
        }

        /*
       A valid post should return 200
        */
        @Test
        fun `post station 200`() {
            val hours = mapOf<DayOfWeek, List<LocalTime>>(
                Pair(
                    DayOfWeek.MONDAY,
                    listOf(
                        LocalTime.parse("09:00", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("20:00", DateTimeFormatter.ISO_TIME)
                    )
                ),
                Pair(
                    DayOfWeek.TUESDAY,
                    listOf(
                        LocalTime.parse("09:00", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("20:00", DateTimeFormatter.ISO_TIME)
                    )
                ),
                Pair(
                    DayOfWeek.WEDNESDAY,
                    listOf(
                        LocalTime.parse("09:00", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("20:00", DateTimeFormatter.ISO_TIME)
                    )
                ),
                Pair(
                    DayOfWeek.THURSDAY,
                    listOf(
                        LocalTime.parse("09:00", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("20:00", DateTimeFormatter.ISO_TIME)
                    )
                ),
                Pair(
                    DayOfWeek.FRIDAY,
                    listOf(
                        LocalTime.parse("09:00", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("20:00", DateTimeFormatter.ISO_TIME)
                    )
                )
            )
            val form = StationPostForm("Haraldrud", hours)
            val expected = Station(1, "Haraldrud", hours)

            every { StationRepository.exists(1) } returns false
            every { StationService.saveStation(form) } returns expected.right()

            testPost("/stations/", json.stringify(StationPostForm.serializer(), form)) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Station.serializer(), expected), response.content)
            }
        }

        /*
    Invalid Days should return a 422 unprocessable
     */
        @Test
        fun `post invalid days 422`() {
            val hours = mapOf<DayOfWeek, List<LocalTime>>(
                Pair(
                    DayOfWeek.MONDAY,
                    listOf(
                        LocalTime.parse("09:00", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("20:00", DateTimeFormatter.ISO_TIME)
                    )
                ),
                Pair(
                    DayOfWeek.TUESDAY,
                    listOf(
                        LocalTime.parse("09:00", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("20:00", DateTimeFormatter.ISO_TIME)
                    )
                ),
                Pair(
                    DayOfWeek.WEDNESDAY,
                    listOf(
                        LocalTime.parse("09:00", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("20:00", DateTimeFormatter.ISO_TIME)
                    )
                ),
                Pair(
                    DayOfWeek.THURSDAY,
                    listOf(
                        LocalTime.parse("09:00", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("20:00", DateTimeFormatter.ISO_TIME)
                    )
                ),
                Pair(
                    DayOfWeek.FRIDAY,
                    listOf(
                        LocalTime.parse("09:00", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("20:00", DateTimeFormatter.ISO_TIME)
                    )
                ),
                Pair(
                    DayOfWeek.SATURDAY,
                    listOf(
                        LocalTime.parse("09:00", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("20:00", DateTimeFormatter.ISO_TIME)
                    )
                )
            )
            val form = StationPostForm("Haraldrud", hours)

            every { StationRepository.exists(1) } returns false
            every { StationService.saveStation(form) } returns ValidationError.Unprocessable("test").left()

            testPost("/stations/", json.stringify(StationPostForm.serializer(), form)) {
                assertEquals(HttpStatusCode.UnprocessableEntity, response.status())
                println(response.content)
            }
        }

        /*
    Check if missing bearer returns a 401 unauthorized
     */
        @Test
        fun `post without bearer 401`() {
            val form = StationPostForm("Haraldrud")

            every { StationRepository.exists(1) } returns false

            testPost("/stations/", json.stringify(StationPostForm.serializer(), form), null) {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }
        }

        /*
    Mangled JWT should return 401
     */
        @Test
        fun `post with invalid bearer 401`() {
            val form = StationPostForm("Haraldrud")

            every { StationRepository.exists(1) } returns false

            testPost(
                "/stations/",
                json.stringify(StationPostForm.serializer(), form),
                JwtMockConfig.regEmployeeBearer.drop(5)
            ) {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }
        }

        /*
    Partners should not be able to post stations
     */
        @Test
        fun `post station as partner 403`() {
            val form = StationPostForm("Haraldrud")

            every { StationRepository.exists(1) } returns false

            testPost("/stations/", json.stringify(StationPostForm.serializer(), form), JwtMockConfig.partnerBearer1) {
                assertEquals(HttpStatusCode.Forbidden, response.status())
            }
        }

        /*
    Reuse station workers should not be able to post stations
     */
        @Test
        fun `post station as reuse station 403`() {
            val form = StationPostForm("Haraldrud")

            every { StationRepository.exists(1) } returns false

            testPost(
                "/stations/",
                json.stringify(StationPostForm.serializer(), form),
                JwtMockConfig.reuseStationBearer
            ) {
                assertEquals(HttpStatusCode.Forbidden, response.status())
            }
        }

        /*
    Invalid JSON should return a 400
     */
        @Test
        fun `post station invalid json 400`() {
            val testJson = """ {"name": "Haraldrud", "test": "test"} """

            every { StationRepository.exists(1) } returns false

            testPost("/stations/", testJson) {
                assertEquals(HttpStatusCode.BadRequest, response.status())
            }
        }
    }

    @Nested
    inner class Patch {

        /*
        Check for 200 when patch is valid
         */
        @Test
        fun `patch station 200`(){
            val form = StationUpdateForm(1, "Test1")
            val initial = Station(1, "Test")
            val expected = Station(1, "Test1")

            every { StationService.getStationById(1) } returns initial.right()
            every { StationService.updateStation(form) } returns expected.right()

            testPatch("/stations/", json.stringify(StationUpdateForm.serializer(), form)){
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Station.serializer(), expected), response.content)
            }
        }

        /*
        Check for 401 when no bearer is present
         */
        @Test
        fun `patch station no bearer 401`(){
            val form = StationUpdateForm(1, "Test")
            testPatch("/stations/", json.stringify(StationUpdateForm.serializer(), form), null){
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }
        }

        /*
        Check for 403 when partner tries to update station
         */
        @Test
        fun `patch station as partner 403`(){
            val form = StationUpdateForm(1, "Test")
            testPatch("/stations/", json.stringify(StationUpdateForm.serializer(), form), JwtMockConfig.partnerBearer2){
                assertEquals(HttpStatusCode.Forbidden, response.status())
            }
        }

        /*
        Check for 403 when station worker tries to update station
         */
        @Test
        fun `patch station as station worker 403`(){
            val form = StationUpdateForm(1, "Test")
            testPatch("/stations/", json.stringify(StationUpdateForm.serializer(), form), JwtMockConfig.reuseStationBearer){
                assertEquals(HttpStatusCode.Forbidden, response.status())
            }
        }

        /*
        Check for 500 when we encounter a serious error
         */
        @Test
        fun `patch station 500`(){
            val form = StationUpdateForm(1, "Test")
            every { StationService.updateStation(form) } returns ServiceError("test").left()

            testPatch("/stations/", json.stringify(StationUpdateForm.serializer(), form)){
                assertEquals(HttpStatusCode.InternalServerError, response.status())
            }
        }

        /*
        Check for 404 when station does not exist
         */
        @Test
        fun `patch station 404`(){
            val form = StationUpdateForm(1, "Test")
            every {StationService.updateStation(form)} returns RepositoryError.NoRowsFound("1").left()

            testPatch("/stations/", json.stringify(StationUpdateForm.serializer(), form)){
                assertEquals(HttpStatusCode.NotFound, response.status())
            }
        }

        /*
        Stations that cannot be deserialized should return a 400
         */
        @Test
        fun `patch station invalid json 400`(){
            val testJson = """ {"id": "NaN", "name": "tester"} """

            testPatch("/stations/", testJson){
                assertEquals(HttpStatusCode.BadRequest, response.status())
            }
        }

        /*
        Check for 422 when form is invalid
         */
        fun `patch station invalid state`(){
            val form = StationUpdateForm(0, "Test")

            testPatch("/stations/2", json.stringify(StationUpdateForm.serializer(), form)){
                assertEquals(HttpStatusCode.UnprocessableEntity, response.status())
            }
        }


    }

    @Nested
    inner class Delete {

        /*
        A successful delete should return the deleted station
         */
        @Test
        fun `delete station 200`() {
            val expected = Station(1, "Test")
            every { StationService.deleteStationById(1) } returns expected.right()

            testDelete("/stations/1" ){
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Station.serializer(), expected), response.content)
            }
        }

        /*
        Attempting to delete a non-existing station should return a 404
         */
        @Test
        fun `delete station 404`() {
            every { StationService.deleteStationById(1) } returns RepositoryError.NoRowsFound("1").left()

            testDelete("/stations/1"){
                assertEquals(HttpStatusCode.NotFound, response.status())
                assertEquals("No rows found with provided ID, 1", response.content)
            }
        }

        /*
        Path parameter value for delete should be greater than 0
         */
        @Test
        fun `delete station 422`() {
            testDelete("/stations/0"){
                assertEquals(HttpStatusCode.UnprocessableEntity, response.status())
                assertEquals("id: Must be greater than 0", response.content)
            }
        }

        /*
        Path parameter value that cannot be parsed to int should return 400
         */
        @Test
        fun `delete station bad path`(){
            testDelete("/stations/asdasd"){
                assertEquals(HttpStatusCode.BadRequest, response.status())
            }
        }

        /*
        Check for 500 when we encounter a serious error
         */
        @Test
        fun `delete station 500`(){
            every { StationService.deleteStationById(1) } returns ServiceError("test").left()

            testDelete("/stations/1"){
                assertEquals(HttpStatusCode.InternalServerError, response.status())
                assertEquals("test", response.content)
            }
        }

        /*
        Partners should not be allowed to delete a station
         */
        @Test
        fun `delete station 403`(){
            testDelete("stations/1", JwtMockConfig.partnerBearer1){
                assertEquals(HttpStatusCode.Forbidden, response.status())
            }
        }

        /*
        Station workers should not be allowed to delete a station
         */
        @Test
        fun `delete station station worker 403`(){
            testDelete("/stations/1", JwtMockConfig.reuseStationBearer){
                assertEquals(HttpStatusCode.Forbidden, response.status())
            }
        }

        /*
        Cannot delete station without being authenticated
         */
        @Test
        fun `delete station without bearer 401`(){
            testDelete("/stations/1", null){
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }
        }


    }
}