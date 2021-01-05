package no.oslokommune.ombruk.uttak.service

import arrow.core.Either
import arrow.core.right
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import no.oslokommune.ombruk.shared.database.initDB
import no.oslokommune.ombruk.uttak.database.UttakRepository
import no.oslokommune.ombruk.uttak.database.GjentakelsesRegelTable
import no.oslokommune.ombruk.uttak.form.UttakGetForm
import no.oslokommune.ombruk.uttak.form.UttakPostForm
import no.oslokommune.ombruk.uttak.model.Uttak
import no.oslokommune.ombruk.uttak.model.GjentakelsesRegel
import no.oslokommune.ombruk.uttak.model.UttaksType
import no.oslokommune.ombruk.uttaksdata.database.UttaksDataRepository
import no.oslokommune.ombruk.uttaksdata.model.UttaksData
import no.oslokommune.ombruk.uttaksdata.service.UttaksDataService
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockKExtension::class)
class UttakServiceTest {

    init {
        initDB()
    }

    @BeforeEach
    fun setup() {
        mockkObject(UttakRepository)
        mockkObject(UttaksDataService)
        mockkObject(GjentakelsesRegelTable)
        mockkObject(UttaksDataRepository)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    @AfterAll
    fun finish() {
        unmockkAll()
    }

    @Nested
    inner class GetUttakTable {
        /**
         * Check that get by id returns the expected uttak.
         */
        @Test
        fun `get by id`(@MockK expectedUttak: Uttak) {
            val id = 1
            every { UttakRepository.getUttakByID(id) } returns expectedUttak.right()

            val actualUttak = UttakService.getUttakByID(id)
            require(actualUttak is Either.Right)

            assertEquals(expectedUttak, actualUttak.b)
        }

        /**
         * Check that get uttak returns the exepected list of uttak
         */
        @Test
        fun `get all`(@MockK expectedUttak: List<Uttak>) {
            every { UttakRepository.getUttak(null) } returns expectedUttak.right()

            val actualUttak = UttakService.getUttak()
            require(actualUttak is Either.Right)

            assertEquals(expectedUttak, actualUttak.b)
        }

        /**
         * Check that we can get the expected uttak when given a stasjon id
         */
        @Test
        fun `get by stasjon id`(@MockK expectedUttak: List<Uttak>) {
            val form = UttakGetForm(stasjonId = 1)
            every { UttakRepository.getUttak(form) } returns expectedUttak.right()

            val actualUttak = UttakService.getUttak(form)
            require(actualUttak is Either.Right)
            assertEquals(expectedUttak, actualUttak.b)
        }

        /**
         * Check that we can get the expected uttak when given a partner id
         */
        @Test
        fun `get by partner id`(@MockK expectedUttak: List<Uttak>) {
            val form = UttakGetForm(partnerId = 1)
            every { UttakRepository.getUttak(form) } returns expectedUttak.right()

            val actualUttak = UttakService.getUttak(form)
            require(actualUttak is Either.Right)
            assertEquals(expectedUttak, actualUttak.b)
        }

        /**
         * Check that we can get the expected uttak when given a stasjon and partner id
         */
        @Test
        fun `get by partner and stasjon id`(@MockK expectedUttak: List<Uttak>) {
            val form = UttakGetForm(partnerId = 1, stasjonId = 1)
            every { UttakRepository.getUttak(form) } returns expectedUttak.right()

            val actualUttak = UttakService.getUttak(form)
            require(actualUttak is Either.Right)
            assertEquals(expectedUttak, actualUttak.b)
        }

        /**
         * Check that we can get the expected uttak when given a date time range
         */
        @Test
        fun `get by datetime range`(@MockK expectedUttak: List<Uttak>) {
            val form = UttakGetForm(
                startTidspunkt = LocalDateTime.parse("2020-08-15T15:30:00", DateTimeFormatter.ISO_DATE_TIME),
                sluttTidspunkt = LocalDateTime.parse("2020-08-20T15:30:00", DateTimeFormatter.ISO_DATE_TIME)
            )
            every { UttakRepository.getUttak(form) } returns expectedUttak.right()

            val actualUttak = UttakService.getUttak(form)
            require(actualUttak is Either.Right)
            assertEquals(expectedUttak, actualUttak.b)
        }

        /**
         * Check that we can get the expected uttak when given a recurrence rule id
         */
        @Test
        fun `get by gjentakelsesRegel id`(@MockK expectedUttak: List<Uttak>) {
            val form = UttakGetForm(gjentakelsesRegelID = 1)
            every { UttakRepository.getUttak(form) } returns expectedUttak.right()

            val actualUttak = UttakService.getUttak(form)
            require(actualUttak is Either.Right)
            assertEquals(expectedUttak, actualUttak.b)
        }

    }

    @Nested
    inner class SaveUttakTable {

        /**
         * Check that save single uttak calls the repository and returns the saved uttak.
         */
        @Test
        fun `save single uttak`(
            @MockK expectedUttak: Uttak,
            @MockK uttaksData: UttaksData,
            @MockK gjentakelsesRegel: GjentakelsesRegel
        ) {
            val from = LocalDateTime.parse("2020-09-01T10:00:00Z", DateTimeFormatter.ISO_DATE_TIME)
            val form = UttakPostForm(
                stasjonId = 1,
                partnerId = 1,
                gjentakelsesRegel = GjentakelsesRegel(
                    dager = listOf(DayOfWeek.TUESDAY),
                    until = from.plusDays(1)
                ),
                type = UttaksType.GJENTAKENDE,
                startTidspunkt = from,
                sluttTidspunkt = from.plusHours(5)
            )

            every { UttakRepository.insertUttak(any()) } returns expectedUttak.right()
            every { UttaksDataService.saveUttaksData(any()) } returns uttaksData.right()
            every { expectedUttak.uttaksData = any() } answers { uttaksData }
            every { GjentakelsesRegelTable.insertGjentakelsesRegel(form.gjentakelsesRegel!!) } returns gjentakelsesRegel.right()

            val actualUttak = UttakService.saveUttak(form)
            require(actualUttak is Either.Right)
            verify(exactly = 1) { UttakRepository.insertUttak(form) }
            verify(exactly = 1) { UttaksDataService.saveUttaksData(actualUttak.b) }
            assertEquals(expectedUttak, actualUttak.b)
        }
    }
}