package no.oslokommune.ombruk.stasjon.service

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkObject
import no.oslokommune.ombruk.stasjon.database.StasjonRepository
import no.oslokommune.ombruk.stasjon.form.StasjonGetForm
import no.oslokommune.ombruk.stasjon.form.StasjonPostForm
import no.oslokommune.ombruk.stasjon.form.StasjonUpdateForm
import no.oslokommune.ombruk.stasjon.model.Stasjon
import no.oslokommune.ombruk.shared.api.KeycloakGroupIntegration
import no.oslokommune.ombruk.shared.database.initDB
import no.oslokommune.ombruk.shared.error.KeycloakIntegrationError
import no.oslokommune.ombruk.shared.error.RepositoryError
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

/*
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockKExtension::class)
class StasjonServiceTest {

    init {
        initDB()
    }

    @BeforeEach
    fun setup() {
        mockkObject(StasjonRepository)
        mockkObject(KeycloakGroupIntegration)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Nested
    inner class Get {

        /**
         * Successful get by ID should return a stasjon
         */
        @Test
        fun `get stasjon by ID`(@MockK expected: Stasjon) {
            every { StasjonRepository.getStasjonById(1) } returns expected.right()

            val actual = StasjonService.getStasjonById(1)
            require(actual is Either.Right)
            assertEquals(expected, actual.b)
        }

        /**
         * Return RepositoryError.NoRowsFound when ID does not exist
         */
        @Test
        fun `get stasjon by invalid ID`(@MockK expected: RepositoryError.NoRowsFound) {
            every { StasjonRepository.getStasjonById(1) } returns expected.left()

            val actual = StasjonService.getStasjonById(1)
            require(actual is Either.Left)
            assertEquals(expected, actual.a)
        }

        /**
         * Getting all stasjoner should return a list of stasjoner
         */
        @Test
        fun `get all stasjoner`(@MockK expected: List<Stasjon>) {
            every { StasjonRepository.getStasjoner(StasjonGetForm()) } returns expected.right()

            val actual = StasjonService.getStasjoner(StasjonGetForm())
            require(actual is Either.Right)
            assertEquals(expected, actual.b)
        }

        /**
         * When getting all stasjoner fail, a RepositoryError.SelectError should be returned
         */
        @Test
        fun `get all stasjoner failure`(@MockK expected: RepositoryError.SelectError) {
            every { StasjonRepository.getStasjoner(StasjonGetForm()) } returns expected.left()

            val actual = StasjonService.getStasjoner(StasjonGetForm())
            require(actual is Either.Left)
            assertEquals(expected, actual.a)
        }
    }

    @Nested
    inner class SaveStasjon {

        /**
         * When a stasjon is successfully saved, the saved stasjon should be returned
         */
        @Test
        fun `save stasjon success`(@MockK(relaxed = true) expected: Stasjon, @MockK form: StasjonPostForm) {
            every { StasjonRepository.insertStasjon(form) } returns expected.right()
            every { KeycloakGroupIntegration.createGroup(expected.navn, expected.id) } returns 1.right()

            val actual = StasjonService.saveStasjon(form)
            require(actual is Either.Right)
            assertEquals(expected, actual.b)
        }

        /**
         * When the stasjon cannot be saved in the database, return a RepositoryError.InsertError
         */
        @Test
        fun `save stasjon repositoryError`(
            @MockK(relaxed = true) expected: RepositoryError.InsertError,
            @MockK form: StasjonPostForm
        ) {
            every { StasjonRepository.insertStasjon(form) } returns expected.left()

            val actual = StasjonService.saveStasjon(form)
            require(actual is Either.Left)
            assertEquals(expected, actual.a)
        }

        /**
         * When the stasjon cannot be saved in keycloak, return a KeycloakIntegrationError
         */
        @Test
        fun `save stasjon keycloakError`(
                @MockK(relaxed = true) stasjon: Stasjon,
                @MockK expected: KeycloakIntegrationError.KeycloakError,
                @MockK form: StasjonPostForm
        ) {
            every { StasjonRepository.insertStasjon(form) } returns stasjon.right()
            every { KeycloakGroupIntegration.createGroup(stasjon.navn, stasjon.id) } returns expected.left()

            val actual = StasjonService.saveStasjon(form)
            require(actual is Either.Left)
            assertEquals(expected, actual.a)
        }
    }

    @Nested
    inner class Delete {

        /**
         * A successfull deletion should return the deleted stasjon
         */
        @Test
        fun `delete stasjon success`(@MockK(relaxed = true) expected: Stasjon) {

            every { StasjonRepository.deleteStasjon(1) } returns 1.right()
            every { StasjonRepository.getStasjonById(1) } returns expected.right()
            every { KeycloakGroupIntegration.deleteGroup(expected.navn) } returns 1.right()

            val actual = StasjonService.deleteStasjonById(1)
            require(actual is Either.Right)
            assertEquals(expected, actual.b)
        }

        /**
         * Failing to delete a stasjon in the database should return a repository error
         */
        @Test
        fun `delete stasjon repository error`(
                @MockK(relaxed = true) stasjon: Stasjon,
                @MockK expected: RepositoryError.DeleteError
        ) {
            every { StasjonRepository.getStasjonById(1) } returns stasjon.right()
            every { StasjonRepository.deleteStasjon(1) } returns expected.left()

            val actual = StasjonService.deleteStasjonById(1)
            require(actual is Either.Left)
            assertEquals(expected, actual.a)
        }

        /**
         * Failing to delete a stasjon in keycloak should return a KeycloakIntegrationError.KeycloakError
         */
        @Test
        fun `delete stasjon keycloak error`(
                @MockK(relaxed = true) stasjon: Stasjon,
                @MockK expected: KeycloakIntegrationError.KeycloakError
        ) {
            every { StasjonRepository.getStasjonById(1) } returns stasjon.right()
            every { StasjonRepository.deleteStasjon(stasjon.id) } returns 1.right()
            every { KeycloakGroupIntegration.deleteGroup(stasjon.navn) } returns expected.left()

            val actual = StasjonService.deleteStasjonById(1)
            require(actual is Either.Left)
            assertEquals(expected, actual.a)
        }
    }

    @Nested
    inner class Update {

        /**
         * A successful update should return the new stasjon
         */
        @Test
        fun `update stasjon success`(
                @MockK(relaxed = true) initial: Stasjon,
                @MockK(relaxed = true) form: StasjonUpdateForm,
                @MockK(relaxed = true) expected: Stasjon
        ) {
            every { StasjonRepository.getStasjonById(form.id) } returns initial.right()
            every { StasjonRepository.updateStasjon(form) } returns expected.right()
            every { KeycloakGroupIntegration.updateGroup(initial.navn, expected.navn) } returns Unit.right()

            val actual = StasjonService.updateStasjon(form)
            require(actual is Either.Right)
            assertEquals(expected, actual.b)
        }

        /**
         * When the stasjon cannot be updated in the database, a RepositoryError.UpdateError should be returned
         */
        @Test
        fun `update stasjon repository error`(
                @MockK(relaxed = true) initial: Stasjon,
                @MockK(relaxed = true) form: StasjonUpdateForm,
                @MockK expected: RepositoryError.UpdateError
        ) {
            every { StasjonRepository.getStasjonById(form.id) } returns initial.right()
            every { StasjonRepository.updateStasjon(form) } returns expected.left()

            val actual = StasjonService.updateStasjon(form)
            require(actual is Either.Left)
            assertEquals(expected, actual.a)
        }

        /**
         * When the stasjon cannot be updated in keycloak, a KeycloakIntegrationError.KeycloakError should be returned
         */
        @Test
        fun `update stasjon keycloak error`(
                @MockK(relaxed = true) initial: Stasjon,
                @MockK(relaxed = true) updated: Stasjon,
                @MockK(relaxed = true) form: StasjonUpdateForm,
                @MockK expected: KeycloakIntegrationError.KeycloakError
        ) {
            every { StasjonRepository.getStasjonById(form.id) } returns initial.right()
            every { StasjonRepository.updateStasjon(form) } returns updated.right()
            every { KeycloakGroupIntegration.updateGroup(initial.navn, updated.navn) } returns expected.left()

            val actual = StasjonService.updateStasjon(form)
            require(actual is Either.Left)
            assertEquals(expected, actual.a)
        }
    }
}
 */