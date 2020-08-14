package reporting.database

import arrow.core.Either
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import ombruk.backend.calendar.database.Events
import ombruk.backend.calendar.database.Stations
import ombruk.backend.calendar.model.Event
import ombruk.backend.calendar.model.Station
import ombruk.backend.partner.database.Partners
import ombruk.backend.partner.model.Partner
import ombruk.backend.reporting.database.ReportRepository
import ombruk.backend.reporting.database.Reports
import ombruk.backend.reporting.form.ReportGetForm
import ombruk.backend.reporting.form.ReportUpdateForm
import ombruk.backend.reporting.model.Report
import ombruk.backend.shared.database.initDB
import ombruk.backend.shared.error.RepositoryError
import ombruk.backend.shared.model.serializer.DayOfWeekSerializer
import ombruk.backend.shared.model.serializer.LocalTimeSerializer
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ReportRepositoryTest {
    lateinit var testPartner: Partner
    lateinit var testPartner2: Partner
    lateinit var testStation: Station
    lateinit var testStation2: Station
    lateinit var testReport: Report
    lateinit var testReport2: Report
    lateinit var testReport3: Report
    lateinit var testEvent: Event
    lateinit var testEvent2: Event
    lateinit var testEvent3: Event
    lateinit var testEvent4: Event
    lateinit var testEvent5: Event
    lateinit var testEvent6: Event

    init {
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

            testPartner2 =
                Partner(
                    testPartnerId2,
                    "TestPartner 2",
                    "Description of TestPartner 2",
                    "911",
                    "example@gmail.com"
                )


            var opensAt = LocalTime.parse("09:00:00Z", DateTimeFormatter.ISO_TIME)!!
            var closesAt = LocalTime.parse("21:00:00Z", DateTimeFormatter.ISO_TIME)!!
            var hours = mapOf(
                Pair(DayOfWeek.MONDAY, listOf(opensAt, closesAt)),
                Pair(DayOfWeek.TUESDAY, listOf(opensAt, closesAt)),
                Pair(DayOfWeek.WEDNESDAY, listOf(opensAt, closesAt)),
                Pair(DayOfWeek.THURSDAY, listOf(opensAt, closesAt)),
                Pair(DayOfWeek.FRIDAY, listOf(opensAt, closesAt))
            )
            val json = Json(JsonConfiguration.Stable)

            val testStationId = Stations.insertAndGetId {
                it[name] = "Test Station 1"
                it[Stations.hours] =
                    json.toJson(MapSerializer(DayOfWeekSerializer, ListSerializer(LocalTimeSerializer)), hours)
                        .toString()
            }.value

            testStation = Station(
                testStationId,
                "Test Station 1",
                hours
            )

            opensAt = LocalTime.parse("08:00:00", DateTimeFormatter.ISO_TIME)
            closesAt = LocalTime.parse("20:00:00", DateTimeFormatter.ISO_TIME)
            hours = mapOf(
                Pair(DayOfWeek.MONDAY, listOf(opensAt, closesAt)),
                Pair(DayOfWeek.TUESDAY, listOf(opensAt, closesAt)),
                Pair(DayOfWeek.WEDNESDAY, listOf(opensAt, closesAt)),
                Pair(DayOfWeek.THURSDAY, listOf(opensAt, closesAt)),
                Pair(DayOfWeek.FRIDAY, listOf(opensAt, closesAt))
            )

            val testStationId2 = Stations.insertAndGetId {
                it[name] = "Test Station 2"
                it[Stations.hours] = json.toJson(
                    MapSerializer(
                        DayOfWeekSerializer, ListSerializer(
                            LocalTimeSerializer
                        )
                    ), hours
                )
                    .toString()
            }.value
            testStation2 = Station(
                testStationId2,
                "Test Station 2",
                hours
            )

            testEvent = Event(
                0,
                LocalDateTime.parse("2020-07-07T16:00:00Z", DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.parse("2020-07-07T18:00:00Z", DateTimeFormatter.ISO_DATE_TIME),
                testStation,
                testPartner
            )
            testEvent = testEvent.copy(id = insertTestEvent(testEvent))
            testEvent2 = Event(
                0,
                LocalDateTime.parse("2020-08-08T16:00:00Z", DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.parse("2020-08-08T17:00:00Z", DateTimeFormatter.ISO_DATE_TIME),
                testStation,
                testPartner
            )
            testEvent2 = testEvent2.copy(id = insertTestEvent(testEvent2))
            testEvent3 = Event(
                0,
                LocalDateTime.parse("2020-05-06T16:00:00Z", DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.parse("2020-05-06T18:00:00Z", DateTimeFormatter.ISO_DATE_TIME),
                testStation,
                testPartner
            )
            testEvent3 = testEvent3.copy(id = insertTestEvent(testEvent3))
            testEvent4 = Event(
                0,
                LocalDateTime.parse("2020-07-07T16:00:00Z", DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.parse("2020-07-07T18:00:00Z", DateTimeFormatter.ISO_DATE_TIME),
                testStation,
                testPartner
            )
            testEvent4 = testEvent4.copy(id = insertTestEvent(testEvent4))
            testEvent5 = Event(
                0,
                LocalDateTime.parse("2020-08-08T16:00:00Z", DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.parse("2020-08-08T17:00:00Z", DateTimeFormatter.ISO_DATE_TIME),
                testStation,
                testPartner
            )
            testEvent5 = testEvent5.copy(id = insertTestEvent(testEvent5))

            testReport = Report(
                1,
                testEvent.id,
                testPartner.id,
                testStation,
                testEvent.startDateTime,
                testEvent.endDateTime
            )

            testReport2 = Report(
                2,
                testEvent2.id,
                testPartner.id,
                testStation2,
                testEvent2.startDateTime,
                testEvent2.endDateTime
            )

            testReport3 = Report(
                3,
                testEvent3.id,
                testPartner2.id,
                testStation,
                testEvent3.startDateTime,
                testEvent3.endDateTime
            )
            testReport = testReport.copy(reportId = insertTestReport(testReport))
            testReport2 = testReport2.copy(reportId = insertTestReport(testReport2))
            testReport3 = testReport3.copy(reportId = insertTestReport(testReport3))
        }
    }

    @AfterAll
    fun cleanPartnersAndStationsFromDB() {
        transaction {
            Partners.deleteAll()
            Stations.deleteAll()
        }
    }

    fun insertTestReport(report: Report) =
        transaction {
            Reports.insertAndGetId {
                it[weight] = null
                it[reportedDateTime] = null
                it[eventID] = report.eventId
                it[partnerID] = report.partnerId
                it[stationID] = report.station.id
                it[startDateTime] = report.startDateTime
                it[endDateTime] = report.endDateTime
            }.value
        }

    fun insertTestEvent(event: Event) = transaction {
        Events.insertAndGetId {
            it[startDateTime] = event.startDateTime
            it[endDateTime] = event.endDateTime
            it[recurrenceRuleID] = event.recurrenceRule?.id
            it[stationID] = event.station.id
            it[partnerID] = event.partner.id
        }.value
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    inner class Get {

        /**
         * Get Report by valid ID returns a report
         */
        @Test
        fun `get Report by valid ID`() {
            val expected = testReport
            val result = ReportRepository.getReportByID(expected.reportId)
            require(result is Either.Right)
            assertEquals(expected, result.b)
        }

        /**
         * Get report by non-existing ID should return RepositoryError.NoRowsFound
         */
        @Test
        fun `get by invalid ID is left`() {
            val expected = RepositoryError.NoRowsFound("ID 0 does not exist!")
            val actual = ReportRepository.getReportByID(0)
            require(actual is Either.Left)
            assertEquals(expected, actual.a)
        }

        @Suppress("unused") // referenced in test
        fun generateValidForms() = listOf(
            Pair(null, listOf(testReport, testReport2, testReport3)),
            Pair(ReportGetForm(), listOf(testReport, testReport2, testReport3)),
            Pair(ReportGetForm(eventId = testReport.eventId), listOf(testReport)),
            Pair(ReportGetForm(eventId = 0), emptyList()),
            Pair(ReportGetForm(stationId = testStation.id), listOf(testReport, testReport3)),
            Pair(ReportGetForm(stationId = 0), emptyList()),
            Pair(ReportGetForm(partnerId = testPartner2.id), listOf(testReport3)),
            Pair(ReportGetForm(partnerId = 0), emptyList()),
            Pair(
                ReportGetForm(fromDate = LocalDateTime.parse("2018-01-01T12:00:00Z", DateTimeFormatter.ISO_DATE_TIME)),
                listOf(testReport, testReport2, testReport3)
            ),
            Pair(
                ReportGetForm(toDate = LocalDateTime.parse("2022-06-03T13:28:00Z", DateTimeFormatter.ISO_DATE_TIME)),
                listOf(testReport, testReport2, testReport3)
            ),
            Pair(
                ReportGetForm(toDate = LocalDateTime.parse("2018-06-03T15:32:00Z", DateTimeFormatter.ISO_DATE_TIME)),
                emptyList()
            ),
            Pair(
                ReportGetForm(fromDate = LocalDateTime.parse("2022-08-13T20:33:00Z", DateTimeFormatter.ISO_DATE_TIME)),
                emptyList()
            ),
            Pair(
                ReportGetForm(fromDate = LocalDateTime.parse("2020-07-07T15:59:00Z", DateTimeFormatter.ISO_DATE_TIME)),
                listOf(testReport, testReport2)
            ),
            Pair(
                ReportGetForm(fromDate = LocalDateTime.parse("2020-07-07T16:00:00Z", DateTimeFormatter.ISO_DATE_TIME)),
                listOf(testReport, testReport2)
            ),
            Pair(
                ReportGetForm(fromDate = LocalDateTime.parse("2020-07-07T16:00:01Z", DateTimeFormatter.ISO_DATE_TIME)),
                listOf(testReport2)
            ),
            Pair(
                ReportGetForm(toDate = LocalDateTime.parse("2020-07-07T17:59:00Z", DateTimeFormatter.ISO_DATE_TIME)),
                listOf(testReport3)
            ),
            Pair(
                ReportGetForm(toDate = LocalDateTime.parse("2020-07-07T18:00:00Z", DateTimeFormatter.ISO_DATE_TIME)),
                listOf(testReport, testReport3)
            ),
            Pair(
                ReportGetForm(toDate = LocalDateTime.parse("2020-07-07T18:00:01Z", DateTimeFormatter.ISO_DATE_TIME)),
                listOf(testReport, testReport3)
            ),
            Pair(
                ReportGetForm(
                    fromDate = LocalDateTime.parse("2020-07-07T13:00:00Z", DateTimeFormatter.ISO_DATE_TIME),
                    toDate = LocalDateTime.parse("2020-08-09T16:00:00Z", DateTimeFormatter.ISO_DATE_TIME)
                ),
                listOf(testReport, testReport2)
            ),
            Pair(
                ReportGetForm(
                    stationId = testStation.id,
                    partnerId = testPartner.id,
                    fromDate = LocalDateTime.parse("2020-06-03T08:00:00Z", DateTimeFormatter.ISO_DATE_TIME),
                    toDate = LocalDateTime.parse("2020-09-10T15:57:00Z", DateTimeFormatter.ISO_DATE_TIME)
                ),
                listOf(testReport)
            )
        )

        @ParameterizedTest
        @MethodSource("generateValidForms")
        fun `valid gets with valid forms`(testData: Pair<ReportGetForm?, List<Report>>) {
            val expected = testData.second
            val actual = ReportRepository.getReports(testData.first)
            require(actual is Either.Right)
            assertEquals(expected, actual.b)
        }
    }

    @Nested
    inner class Update {

        var updateTestReport = Report(
            0,
            testEvent4.id,
            testPartner.id,
            testStation,
            testEvent4.startDateTime,
            testEvent4.endDateTime
        )

        var updateTestReport2 = Report(
            0,
            testEvent5.id,
            testPartner.id,
            testStation2,
            testEvent5.startDateTime,
            testEvent5.endDateTime
        )

//        var updateTestReport3 = Report(
//            0,
//            6,
//            testPartner2.id,
//            testStation,
//            LocalDateTime.parse("2020-05-06T16:00:00Z", DateTimeFormatter.ISO_DATE_TIME),
//            LocalDateTime.parse("2020-05-06T18:00:00Z", DateTimeFormatter.ISO_DATE_TIME)
//        )

        init {
            updateTestReport = updateTestReport.copy(reportId = insertTestReport(updateTestReport))
            updateTestReport2 = updateTestReport2.copy(reportId = insertTestReport(updateTestReport2))
//            updateTestReport3 = updateTestReport3.copy(reportId = insertTestReport(updateTestReport3))
        }

        /**
         * Valid update
         */
        @Test
        fun `Update report valid`() {
            val expectedWeight = 50
            val form = ReportUpdateForm(updateTestReport.reportId, expectedWeight)
            val result = ReportRepository.updateReport(form)
            require(result is Either.Right)
            val newReport =
                updateTestReport.copy(reportedDateTime = result.b.reportedDateTime, weight = result.b.weight)
            assertEquals(newReport, result.b)
        }

        /**
         * Non-existing ID's should return a RepositoryError.NoRowsFound
         */
        @Test
        fun `Update report invalid ID`() {
            val form = ReportUpdateForm(0, 50)
            val expected = RepositoryError.NoRowsFound("ID 0 does not exist!")

            val result = ReportRepository.updateReport(form)
            require(result is Either.Left)
            assert(result.a is RepositoryError.NoRowsFound)
            assertEquals(expected, result.a)
        }

        /**
         * Attempting to set a weight that's lesser than 1 should result in a RepositoryError.UpdateError
         */
        @Test
        fun `update report with invalid weight`() {
            val form = ReportUpdateForm(testReport2.reportId, 0)
            val expected = RepositoryError.UpdateError("Failed to update report")

            val initial = ReportRepository.getReportByID(testReport2.reportId)
            require(initial is Either.Right)

            val result = ReportRepository.updateReport(form)
            require(result is Either.Left)
            assertEquals(expected, result.a)
            val after = ReportRepository.getReportByID(testReport2.reportId)
            require(after is Either.Right)
            assertEquals(initial.b, after.b)
        }

        /**
         * Test automatic update with event
         */
        @Test
        fun `update report with event valid`() {
            val event = Event(
                testReport2.eventId,
                LocalDateTime.parse("2020-05-05T16:00:00Z", DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.parse("2020-05-05T18:00:00Z", DateTimeFormatter.ISO_DATE_TIME),
                testStation,
                testPartner
            )
            val expected = testReport2.copy(startDateTime = event.startDateTime, endDateTime = event.endDateTime)

            val result = ReportRepository.updateReport(event)
            require(result is Either.Right)
            val actual = ReportRepository.getReportByID(expected.reportId)
            require(actual is Either.Right)
            assertEquals(expected, actual.b)
        }

        /**
         * Test automatic update with event where eventId does not exist
         */
        @Test
        fun `update report with event invalid eventId`() {
            val event = Event(
                0,
                LocalDateTime.parse("2020-05-05T16:00:00Z", DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.parse("2020-05-05T18:00:00Z", DateTimeFormatter.ISO_DATE_TIME),
                testStation,
                testPartner
            )
            val expected = RepositoryError.NoRowsFound("eventId 0 does not exist!")

            val actual = ReportRepository.updateReport(event)
            require(actual is Either.Left)
            assertEquals(expected, actual.a)
        }

    }

    @Nested
    inner class Insert {

        /**
         * Valid insertion should return a report
         */
        @Test
        fun `insert report valid`() {
            val actual = ReportRepository.insertReport(testEvent5)
            require(actual is Either.Right)
            assertEquals(testEvent5.id, actual.b.eventId)
            assertEquals(testEvent5.partner.id, actual.b.partnerId)
            assertEquals(testEvent5.station, actual.b.station)
            assertEquals(testEvent5.startDateTime, actual.b.startDateTime)
            assertEquals(testEvent5.endDateTime, actual.b.endDateTime)
            assert(actual.b.reportedDateTime == null)
            assert(actual.b.weight == null)
        }
    }
}