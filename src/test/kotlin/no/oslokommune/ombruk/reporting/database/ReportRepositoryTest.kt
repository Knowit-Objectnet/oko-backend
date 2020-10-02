package no.oslokommune.ombruk.uttaksdata.database

import arrow.core.Either
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import no.oslokommune.ombruk.uttak.database.UttakTable
import no.oslokommune.ombruk.stasjon.database.Stasjoner
import no.oslokommune.ombruk.uttak.model.Uttak
import no.oslokommune.ombruk.stasjon.model.Stasjon
import no.oslokommune.ombruk.partner.database.Partners
import no.oslokommune.ombruk.partner.model.Partner
import no.oslokommune.ombruk.uttaksdata.form.ReportGetForm
import no.oslokommune.ombruk.uttaksdata.form.ReportUpdateForm
import no.oslokommune.ombruk.uttaksdata.model.Report
import no.oslokommune.ombruk.shared.database.initDB
import no.oslokommune.ombruk.shared.error.RepositoryError
import no.oslokommune.ombruk.shared.model.serializer.DayOfWeekSerializer
import no.oslokommune.ombruk.shared.model.serializer.LocalTimeSerializer
import no.oslokommune.ombruk.uttaksdata.database.ReportRepository
import no.oslokommune.ombruk.uttaksdata.database.Reports
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
    lateinit var testStasjon: Stasjon
    lateinit var testStasjon2: Stasjon
    lateinit var testReport: Report
    lateinit var testReport2: Report
    lateinit var testReport3: Report
    lateinit var testUttak: Uttak
    lateinit var testUttak2: Uttak
    lateinit var testUttak3: Uttak
    lateinit var testUttak4: Uttak
    lateinit var testUttak5: Uttak
    lateinit var testUttak6: Uttak

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

            val testStasjonId = Stasjoner.insertAndGetId {
                it[name] = "Test Stasjon 1"
                it[Stasjoner.hours] =
                    json.toJson(MapSerializer(DayOfWeekSerializer, ListSerializer(LocalTimeSerializer)), hours)
                        .toString()
            }.value

            testStasjon = Stasjon(
                testStasjonId,
                "Test Stasjon 1",
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

            val testStasjonId2 = Stasjoner.insertAndGetId {
                it[name] = "Test Stasjon 2"
                it[Stasjoner.hours] = json.toJson(
                    MapSerializer(
                        DayOfWeekSerializer, ListSerializer(
                            LocalTimeSerializer
                        )
                    ), hours
                )
                    .toString()
            }.value
            testStasjon2 = Stasjon(
                testStasjonId2,
                "Test Stasjon 2",
                hours
            )

            testUttak = Uttak(
                0,
                LocalDateTime.parse("2020-07-07T16:00:00Z", DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.parse("2020-07-07T18:00:00Z", DateTimeFormatter.ISO_DATE_TIME),
                testStasjon,
                testPartner
            )
            testUttak = testUttak.copy(id = insertTestUttak(testUttak))
            testUttak2 = Uttak(
                0,
                LocalDateTime.parse("2020-08-08T16:00:00Z", DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.parse("2020-08-08T17:00:00Z", DateTimeFormatter.ISO_DATE_TIME),
                testStasjon,
                testPartner
            )
            testUttak2 = testUttak2.copy(id = insertTestUttak(testUttak2))
            testUttak3 = Uttak(
                0,
                LocalDateTime.parse("2020-05-06T16:00:00Z", DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.parse("2020-05-06T18:00:00Z", DateTimeFormatter.ISO_DATE_TIME),
                testStasjon,
                testPartner
            )
            testUttak3 = testUttak3.copy(id = insertTestUttak(testUttak3))
            testUttak4 = Uttak(
                0,
                LocalDateTime.parse("2020-07-07T16:00:00Z", DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.parse("2020-07-07T18:00:00Z", DateTimeFormatter.ISO_DATE_TIME),
                testStasjon,
                testPartner
            )
            testUttak4 = testUttak4.copy(id = insertTestUttak(testUttak4))
            testUttak5 = Uttak(
                0,
                LocalDateTime.parse("2020-08-08T16:00:00Z", DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.parse("2020-08-08T17:00:00Z", DateTimeFormatter.ISO_DATE_TIME),
                testStasjon,
                testPartner
            )
            testUttak5 = testUttak5.copy(id = insertTestUttak(testUttak5))

            testReport = Report(
                1,
                testUttak.id,
                testPartner.id,
                testStasjon,
                testUttak.startDateTime,
                testUttak.endDateTime
            )

            testReport2 = Report(
                2,
                testUttak2.id,
                testPartner.id,
                testStasjon2,
                testUttak2.startDateTime,
                testUttak2.endDateTime
            )

            testReport3 = Report(
                3,
                testUttak3.id,
                testPartner2.id,
                testStasjon,
                testUttak3.startDateTime,
                testUttak3.endDateTime
            )
            testReport = testReport.copy(uttaksdataId = insertTestReport(testReport))
            testReport2 = testReport2.copy(uttaksdataId = insertTestReport(testReport2))
            testReport3 = testReport3.copy(uttaksdataId = insertTestReport(testReport3))
        }
    }

    @AfterAll
    fun cleanPartnersAndStasjonerFromDB() {
        transaction {
            Partners.deleteAll()
            Stasjoner.deleteAll()
        }
    }

    fun insertTestReport(uttaksdata: Report) =
        transaction {
            Reports.insertAndGetId {
                it[weight] = null
                it[uttaksdataedDateTime] = null
                it[uttakID] = uttaksdata.uttakId
                it[partnerID] = uttaksdata.partnerId
                it[stasjonID] = uttaksdata.stasjon.id
                it[startDateTime] = uttaksdata.startDateTime
                it[endDateTime] = uttaksdata.endDateTime
            }.value
        }

    fun insertTestUttak(uttak: Uttak) = transaction {
        UttakTable.insertAndGetId {
            it[startDateTime] = uttak.startDateTime
            it[endDateTime] = uttak.endDateTime
            it[gjentakelsesRegelID] = uttak.gjentakelsesRegel?.id
            it[stasjonID] = uttak.stasjon.id
            it[partnerID] = uttak.partner?.id
        }.value
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    inner class Get {

        /**
         * Get Report by valid ID returns a uttaksdata
         */
        @Test
        fun `get Report by valid ID`() {
            val expected = testReport
            val result = ReportRepository.getReportByID(expected.uttaksdataId)
            require(result is Either.Right)
            assertEquals(expected, result.b)
        }

        /**
         * Get uttaksdata by non-existing ID should return RepositoryError.NoRowsFound
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
            Pair(ReportGetForm(uttakId = testReport.uttakId), listOf(testReport)),
            Pair(ReportGetForm(uttakId = 0), emptyList()),
            Pair(ReportGetForm(stasjonId = testStasjon.id), listOf(testReport, testReport3)),
            Pair(ReportGetForm(stasjonId = 0), emptyList()),
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
                    stasjonId = testStasjon.id,
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
            testUttak4.id,
            testPartner.id,
            testStasjon,
            testUttak4.startDateTime,
            testUttak4.endDateTime
        )

        var updateTestReport2 = Report(
            0,
            testUttak5.id,
            testPartner.id,
            testStasjon2,
            testUttak5.startDateTime,
            testUttak5.endDateTime
        )

//        var updateTestReport3 = Report(
//            0,
//            6,
//            testPartner2.id,
//            testStasjon,
//            LocalDateTime.parse("2020-05-06T16:00:00Z", DateTimeFormatter.ISO_DATE_TIME),
//            LocalDateTime.parse("2020-05-06T18:00:00Z", DateTimeFormatter.ISO_DATE_TIME)
//        )

        init {
            updateTestReport = updateTestReport.copy(uttaksdataId = insertTestReport(updateTestReport))
            updateTestReport2 = updateTestReport2.copy(uttaksdataId = insertTestReport(updateTestReport2))
//            updateTestReport3 = updateTestReport3.copy(uttaksdataId = insertTestReport(updateTestReport3))
        }

        /**
         * Valid update
         */
        @Test
        fun `Update uttaksdata valid`() {
            val expectedWeight = 50
            val form = ReportUpdateForm(updateTestReport.uttaksdataId, expectedWeight)
            val result = ReportRepository.updateReport(form)
            require(result is Either.Right)
            val newReport =
                updateTestReport.copy(uttaksdataedDateTime = result.b.uttaksdataedDateTime, weight = result.b.weight)
            assertEquals(newReport, result.b)
        }

        /**
         * Non-existing ID's should return a RepositoryError.NoRowsFound
         */
        @Test
        fun `Update uttaksdata invalid ID`() {
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
        fun `update uttaksdata with invalid weight`() {
            val form = ReportUpdateForm(testReport2.uttaksdataId, 0)
            val expected = RepositoryError.UpdateError("Failed to update uttaksdata")

            val initial = ReportRepository.getReportByID(testReport2.uttaksdataId)
            require(initial is Either.Right)

            val result = ReportRepository.updateReport(form)
            require(result is Either.Left)
            assertEquals(expected, result.a)
            val after = ReportRepository.getReportByID(testReport2.uttaksdataId)
            require(after is Either.Right)
            assertEquals(initial.b, after.b)
        }

        /**
         * Test automatic update with uttak
         */
        @Test
        fun `update uttaksdata with uttak valid`() {
            val uttak = Uttak(
                testReport2.uttakId,
                LocalDateTime.parse("2020-05-05T16:00:00Z", DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.parse("2020-05-05T18:00:00Z", DateTimeFormatter.ISO_DATE_TIME),
                testStasjon,
                testPartner
            )
            val expected = testReport2.copy(startDateTime = uttak.startDateTime, endDateTime = uttak.endDateTime)

            val result = ReportRepository.updateReport(uttak)
            require(result is Either.Right)
            val actual = ReportRepository.getReportByID(expected.uttaksdataId)
            require(actual is Either.Right)
            assertEquals(expected, actual.b)
        }

        /**
         * Test automatic update with uttak where uttakId does not exist
         */
        @Test
        fun `update uttaksdata with uttak invalid uttakId`() {
            val uttak = Uttak(
                0,
                LocalDateTime.parse("2020-05-05T16:00:00Z", DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.parse("2020-05-05T18:00:00Z", DateTimeFormatter.ISO_DATE_TIME),
                testStasjon,
                testPartner
            )
            val expected = RepositoryError.NoRowsFound("uttakId 0 does not exist!")

            val actual = ReportRepository.updateReport(uttak)
            require(actual is Either.Left)
            assertEquals(expected, actual.a)
        }

    }

    @Nested
    inner class Insert {

        /**
         * Valid insertion should return a uttaksdata
         */
        @Test
        fun `insert uttaksdata valid`() {
            val actual = ReportRepository.insertReport(testUttak5)
            require(actual is Either.Right)
            assertEquals(testUttak5.id, actual.b.uttakId)
            assertEquals(testUttak5.partner?.id, actual.b.partnerId)
            assertEquals(testUttak5.stasjon, actual.b.stasjon)
            assertEquals(testUttak5.startDateTime, actual.b.startDateTime)
            assertEquals(testUttak5.endDateTime, actual.b.endDateTime)
            assert(actual.b.uttaksdataedDateTime == null)
            assert(actual.b.weight == null)
        }
    }
}