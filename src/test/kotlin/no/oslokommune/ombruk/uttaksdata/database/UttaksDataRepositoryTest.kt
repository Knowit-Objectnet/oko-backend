package no.oslokommune.ombruk.uttaksdata.database

import arrow.core.Either
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import no.oslokommune.ombruk.partner.database.PartnerRepository
import no.oslokommune.ombruk.partner.database.Partnere
import no.oslokommune.ombruk.partner.model.Partner
import no.oslokommune.ombruk.shared.database.initDB
import no.oslokommune.ombruk.shared.error.RepositoryError
import no.oslokommune.ombruk.shared.model.serializer.DayOfWeekSerializer
import no.oslokommune.ombruk.shared.model.serializer.LocalTimeSerializer
import no.oslokommune.ombruk.stasjon.database.StasjonRepository
import no.oslokommune.ombruk.stasjon.database.Stasjoner
import no.oslokommune.ombruk.stasjon.model.Stasjon
import no.oslokommune.ombruk.uttak.database.UttakTable
import no.oslokommune.ombruk.uttak.model.Uttak
import no.oslokommune.ombruk.uttaksdata.form.UttaksDataGetForm
import no.oslokommune.ombruk.uttaksdata.form.UttaksDataUpdateForm
import no.oslokommune.ombruk.uttaksdata.model.UttaksData
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
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
class UttaksDataRepositoryTest {
    lateinit var testPartner: Partner
    lateinit var testPartner2: Partner
    lateinit var testStasjon: Stasjon
    lateinit var testStasjon2: Stasjon
    lateinit var testUttaksData: UttaksData
    lateinit var testUttaksData2: UttaksData
    lateinit var testUttaksData3: UttaksData
    lateinit var testUttak: Uttak
    lateinit var testUttak2: Uttak
    lateinit var testUttak3: Uttak
    lateinit var testUttak4: Uttak
    lateinit var testUttak5: Uttak

    lateinit var testUttak7: Uttak

    init {
        initDB()
        transaction {
            val testPartnerId = Partnere.insertAndGetId {
                it[navn] = "TestPartner 1"
                it[beskrivelse] = "Description of TestPartner 1"
                it[telefon] = "+47 2381931"
                it[epost] = "example@gmail.com"
                it[endretTidspunkt] = LocalDateTime.now()
            }.value

            testPartner =
                Partner(
                    testPartnerId,
                    "TestPartner 1",
                    "Description of TestPartner 1",
                    "+47 2381931",
                    "example@gmail.com"
                )

            val testPartnerId2 = Partnere.insertAndGetId {
                it[navn] = "TestPartner 2"
                it[beskrivelse] = "Description of TestPartner 2"
                it[telefon] = "911"
                it[epost] = "example@gmail.com"
                it[endretTidspunkt] = LocalDateTime.now()
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
                it[navn] = "Test Stasjon 1"
                it[endretTidspunkt] = LocalDateTime.now()
                it[aapningstider] =
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
                it[navn] = "Test Stasjon 2"
                it[endretTidspunkt] = LocalDateTime.now()
                it[Stasjoner.aapningstider] = json.toJson(
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
                testStasjon2,
                testPartner
            )
            testUttak2 = testUttak2.copy(id = insertTestUttak(testUttak2))
            testUttak3 = Uttak(
                0,
                LocalDateTime.parse("2020-05-06T16:00:00Z", DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.parse("2020-05-06T18:00:00Z", DateTimeFormatter.ISO_DATE_TIME),
                testStasjon,
                testPartner2
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
            testUttak7 = testUttak5.copy(id = insertTestUttak(testUttak5))

            testUttaksData = UttaksData(
                testUttak.id,
                150,
                testUttak.startTidspunkt
            )

            testUttaksData2 = UttaksData(
                testUttak2.id,
                150,
                testUttak2.startTidspunkt
            )

            testUttaksData3 = UttaksData(
                testUttak3.id,
                150,
                testUttak3.startTidspunkt
            )

            insertTestReport(testUttaksData)
            insertTestReport(testUttaksData2)
            insertTestReport(testUttaksData3)
        }
    }

    @AfterAll
    fun cleanPartnereAndStasjonerFromDB() {
        transaction {
            PartnerRepository.deleteAllPartnere()
            StasjonRepository.deleteAllStasjoner()
        }
    }

    fun insertTestReport(uttaksData: UttaksData) =
        transaction {
            UttaksDataTable.insert {
                it[vekt] = uttaksData.vekt
                it[uttakId] = uttaksData.uttakId
                it[rapportertTidspunkt] = uttaksData.rapportertTidspunkt!!
            }
        }

    fun insertTestUttak(uttak: Uttak) = transaction {
        UttakTable.insertAndGetId {
            it[startTidspunkt] = uttak.startTidspunkt
            it[sluttTidspunkt] = uttak.sluttTidspunkt
            it[gjentakelsesRegelID] = uttak.gjentakelsesRegel?.id
            it[endretTidspunkt] = LocalDateTime.now()
            it[stasjonID] = uttak.stasjon.id
            it[partnerID] = uttak.partner?.id
            it[type] = uttak.type
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
            var expected = testUttaksData
            val result = UttaksDataRepository.getUttaksDataById(expected.uttakId)
            require(result is Either.Right)
            expected = expected.copy(rapportertTidspunkt = result.b.rapportertTidspunkt)
            assertEquals(expected, result.b)
        }

        /**
         * Get uttaksdata by non-existing ID should return RepositoryError.NoRowsFound
         */
        @Test
        fun `get by invalid ID is left`() {
            val expected = RepositoryError.NoRowsFound("ID 0 does not exist!")
            val actual = UttaksDataRepository.getUttaksDataById(0)
            require(actual is Either.Left)
            assertEquals(expected, actual.a)
        }

        @Suppress("unused") // referenced in test
        fun generateValidForms() = listOf(
            Pair(null, listOf(testUttaksData, testUttaksData2, testUttaksData3)),
            Pair(UttaksDataGetForm(), listOf(testUttaksData, testUttaksData2, testUttaksData3)),
            Pair(UttaksDataGetForm(uttakId = testUttaksData.uttakId), listOf(testUttaksData)),
            Pair(UttaksDataGetForm(uttakId = 0), emptyList()),
            Pair(UttaksDataGetForm(stasjonId = testStasjon.id), listOf(testUttaksData, testUttaksData3)),
            Pair(UttaksDataGetForm(stasjonId = 0), emptyList()),
            Pair(UttaksDataGetForm(partnerId = testPartner2.id), listOf(testUttaksData3)),
            Pair(UttaksDataGetForm(partnerId = 0), emptyList()),
            Pair(
                UttaksDataGetForm(
                    fraRapportertTidspunkt = LocalDateTime.parse(
                        "2018-01-01T12:00:00Z",
                        DateTimeFormatter.ISO_DATE_TIME
                    )
                ),
                listOf(testUttaksData, testUttaksData2, testUttaksData3)
            ),
            Pair(
                UttaksDataGetForm(
                    tilRapportertTidspunkt = LocalDateTime.parse(
                        "2022-06-03T13:28:00Z",
                        DateTimeFormatter.ISO_DATE_TIME
                    )
                ),
                listOf(testUttaksData, testUttaksData2, testUttaksData3)
            ),
            Pair(
                UttaksDataGetForm(
                    tilRapportertTidspunkt = LocalDateTime.parse(
                        "2018-06-03T15:32:00Z",
                        DateTimeFormatter.ISO_DATE_TIME
                    )
                ),
                emptyList()
            ),
            Pair(
                UttaksDataGetForm(
                    fraRapportertTidspunkt = LocalDateTime.parse(
                        "2022-08-13T20:33:00Z",
                        DateTimeFormatter.ISO_DATE_TIME
                    )
                ),
                emptyList()
            ),
            Pair(
                UttaksDataGetForm(
                    fraRapportertTidspunkt = LocalDateTime.parse(
                        "2020-07-07T15:59:00Z",
                        DateTimeFormatter.ISO_DATE_TIME
                    )
                ),
                listOf(testUttaksData, testUttaksData2)
            ),
            Pair(
                UttaksDataGetForm(
                    fraRapportertTidspunkt = LocalDateTime.parse(
                        "2020-07-07T16:00:00Z",
                        DateTimeFormatter.ISO_DATE_TIME
                    )
                ),
                listOf(testUttaksData, testUttaksData2)
            ),
            Pair(
                UttaksDataGetForm(
                    fraRapportertTidspunkt = LocalDateTime.parse(
                        "2020-07-07T16:00:01Z",
                        DateTimeFormatter.ISO_DATE_TIME
                    )
                ),
                listOf(testUttaksData2)
            ),
            Pair(
                UttaksDataGetForm(
                    tilRapportertTidspunkt = LocalDateTime.parse(
                        "2020-07-07T15:59:00Z",
                        DateTimeFormatter.ISO_DATE_TIME
                    )
                ),
                listOf(testUttaksData3)
            ),
            Pair(
                UttaksDataGetForm(
                    tilRapportertTidspunkt = LocalDateTime.parse(
                        "2020-07-07T16:00:00Z",
                        DateTimeFormatter.ISO_DATE_TIME
                    )
                ),
                listOf(testUttaksData, testUttaksData3)
            ),
            Pair(
                UttaksDataGetForm(
                    tilRapportertTidspunkt = LocalDateTime.parse(
                        "2020-07-07T16:00:01Z",
                        DateTimeFormatter.ISO_DATE_TIME
                    )
                ),
                listOf(testUttaksData, testUttaksData3)
            ),
            Pair(
                UttaksDataGetForm(
                    fraRapportertTidspunkt = LocalDateTime.parse(
                        "2020-07-07T13:00:00Z",
                        DateTimeFormatter.ISO_DATE_TIME
                    ),
                    tilRapportertTidspunkt = LocalDateTime.parse(
                        "2020-08-09T16:00:00Z",
                        DateTimeFormatter.ISO_DATE_TIME
                    )
                ),
                listOf(testUttaksData, testUttaksData2)
            ),
            Pair(
                UttaksDataGetForm(
                    stasjonId = testStasjon.id,
                    partnerId = testPartner.id,
                    fraRapportertTidspunkt = LocalDateTime.parse(
                        "2020-06-03T08:00:00Z",
                        DateTimeFormatter.ISO_DATE_TIME
                    ),
                    tilRapportertTidspunkt = LocalDateTime.parse(
                        "2020-09-10T15:57:00Z",
                        DateTimeFormatter.ISO_DATE_TIME
                    )
                ),
                listOf(testUttaksData)
            )
        )

        @ParameterizedTest
        @MethodSource("generateValidForms")
        fun `valid gets with valid forms`(testData: Pair<UttaksDataGetForm?, List<UttaksData>>) {
            var expected = testData.second
            val actual = UttaksDataRepository.getUttaksData(testData.first)
            require(actual is Either.Right)
            assertEquals(expected, actual.b)
        }
    }

    @Nested
    inner class Update {

        var updateTestReport = UttaksData(
            testUttak4.id,
            testPartner.id,
            testUttak4.startTidspunkt
        )

        var updateTestReport2 = UttaksData(
            testUttak5.id,
            testPartner.id,
            testUttak5.startTidspunkt
        )

        var updateTestReport3 = UttaksData(
            6,
            testPartner2.id,
            LocalDateTime.parse("2020-05-06T16:00:00Z", DateTimeFormatter.ISO_DATE_TIME)
        )

        init {
            insertTestReport(updateTestReport)
            insertTestReport(updateTestReport2)
            insertTestReport(updateTestReport3)
        }

        /**
         * Valid update
         */
        @Test
        fun `Update uttaksdata valid`() {
            val expectedWeight = 50
            val form = UttaksDataUpdateForm(updateTestReport.uttakId, expectedWeight)
            val result = UttaksDataRepository.updateUttaksData(form)
            require(result is Either.Right)
            val newReport =
                updateTestReport.copy(rapportertTidspunkt = result.b.rapportertTidspunkt, vekt = result.b.vekt)
            assertEquals(newReport, result.b)
        }

        /**
         * Non-existing ID's should return a RepositoryError.NoRowsFound
         */
        @Test
        fun `Update uttaksdata invalid ID`() {
            val form = UttaksDataUpdateForm(0, 50)
            val expected = RepositoryError.NoRowsFound("ID 0 does not exist!")

            val result = UttaksDataRepository.updateUttaksData(form)
            require(result is Either.Left)
            assert(result.a is RepositoryError.NoRowsFound)
            assertEquals(expected, result.a)
        }

        /**
         * Attempting to set a weight that's lesser than 1 should result in a RepositoryError.UpdateError
         */
        @Test
        fun `update uttaksdata with invalid weight`() {
            val form = UttaksDataUpdateForm(testUttaksData2.uttakId, 0)
            val expected = RepositoryError.UpdateError("Failed to update uttaksdata")

            val initial = UttaksDataRepository.getUttaksDataById(testUttaksData2.uttakId)
            require(initial is Either.Right)

            val result = UttaksDataRepository.updateUttaksData(form)
            require(result is Either.Left)
            assertEquals(expected, result.a)
            val after = UttaksDataRepository.getUttaksDataById(testUttaksData2.uttakId)
            require(after is Either.Right)
            assertEquals(initial.b, after.b)
        }

    }

    @Nested
    inner class Insert {

        /**
         * Valid insertion should return a uttaksdata
         */
        @Test
        fun `insert uttaksdata valid`() {
            var uttak = Uttak(
                0,
                LocalDateTime.parse("2020-07-07T16:00:00Z", DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.parse("2020-07-07T18:00:00Z", DateTimeFormatter.ISO_DATE_TIME),
                testStasjon,
                testPartner
            )
            uttak = uttak.copy(id = insertTestUttak(uttak))
            val actual = UttaksDataRepository.insertUttaksdata(uttak)
            actual.fold({ print(it.message) }, { it })
            require(actual is Either.Right)
            assertEquals(uttak.id, actual.b.uttakId)
            assert(actual.b.rapportertTidspunkt == null)
            assert(actual.b.vekt == null)
        }
    }

}