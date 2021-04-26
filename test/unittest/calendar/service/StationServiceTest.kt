//package calendar.service
//
//import arrow.core.Either
//import arrow.core.left
//import arrow.core.right
//import io.mockk.clearAllMocks
//import io.mockk.every
//import io.mockk.impl.annotations.MockK
//import io.mockk.junit5.MockKExtension
//import io.mockk.mockkObject
//import ombruk.backend.calendar.database.StationRepository
//import ombruk.backend.calendar.form.station.StationGetForm
//import ombruk.backend.calendar.form.station.StationPostForm
//import ombruk.backend.calendar.form.station.StationUpdateForm
//import ombruk.backend.calendar.model.Station
//import ombruk.backend.calendar.service.StationService
//import ombruk.backend.shared.api.KeycloakGroupIntegration
//import ombruk.backend.shared.database.initDB
//import ombruk.backend.shared.error.KeycloakIntegrationError
//import ombruk.backend.shared.error.RepositoryError
//import org.junit.jupiter.api.*
//import org.junit.jupiter.api.extension.ExtendWith
//import kotlin.test.assertEquals
//
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@ExtendWith(MockKExtension::class)
//class StationServiceTest {
//
//    init {
//        initDB()
//    }
//
//    @BeforeEach
//    fun setup() {
//        mockkObject(StationRepository)
//        mockkObject(KeycloakGroupIntegration)
//    }
//
//    @AfterEach
//    fun tearDown() {
//        clearAllMocks()
//    }
//
//    @Nested
//    inner class Get {
//
//        /**
//         * Successful get by ID should return a station
//         */
//        @Test
//        fun `get station by ID`(@MockK expected: Station) {
//            every { StationRepository.getStationById(1) } returns expected.right()
//
//            val actual = StationService.getStationById(1)
//            require(actual is Either.Right)
//            assertEquals(expected, actual.b)
//        }
//
//        /**
//         * Return RepositoryError.NoRowsFound when ID does not exist
//         */
//        @Test
//        fun `get station by invalid ID`(@MockK expected: RepositoryError.NoRowsFound) {
//            every { StationRepository.getStationById(1) } returns expected.left()
//
//            val actual = StationService.getStationById(1)
//            require(actual is Either.Left)
//            assertEquals(expected, actual.a)
//        }
//
//        /**
//         * Getting all stations should return a list of stations
//         */
//        @Test
//        fun `get all stations`(@MockK expected: List<Station>) {
//            every { StationRepository.getStations(StationGetForm()) } returns expected.right()
//
//            val actual = StationService.getStations(StationGetForm())
//            require(actual is Either.Right)
//            assertEquals(expected, actual.b)
//        }
//
//        /**
//         * When getting all stations fail, a RepositoryError.SelectError should be returned
//         */
//        @Test
//        fun `get all stations failure`(@MockK expected: RepositoryError.SelectError) {
//            every { StationRepository.getStations(StationGetForm()) } returns expected.left()
//
//            val actual = StationService.getStations(StationGetForm())
//            require(actual is Either.Left)
//            assertEquals(expected, actual.a)
//        }
//    }
//
//    @Nested
//    inner class SaveStation {
//
//        /**
//         * When a station is successfully saved, the saved station should be returned
//         */
//        @Test
//        fun `save station success`(@MockK(relaxed = true) expected: Station, @MockK form: StationPostForm) {
//            every { StationRepository.insertStation(form) } returns expected.right()
//            every { KeycloakGroupIntegration.createGroup(expected.name, expected.id) } returns 1.right()
//
//            val actual = StationService.saveStation(form)
//            require(actual is Either.Right)
//            assertEquals(expected, actual.b)
//        }
//
//        /**
//         * When the station cannot be saved in the database, return a RepositoryError.InsertError
//         */
//        @Test
//        fun `save station repositoryError`(
//            @MockK(relaxed = true) expected: RepositoryError.InsertError,
//            @MockK form: StationPostForm
//        ) {
//            every { StationRepository.insertStation(form) } returns expected.left()
//
//            val actual = StationService.saveStation(form)
//            require(actual is Either.Left)
//            assertEquals(expected, actual.a)
//        }
//
//        /**
//         * When the station cannot be saved in keycloak, return a KeycloakIntegrationError
//         */
//        @Test
//        fun `save station keycloakError`(
//            @MockK(relaxed = true) station: Station,
//            @MockK expected: KeycloakIntegrationError.KeycloakError,
//            @MockK form: StationPostForm
//        ) {
//            every { StationRepository.insertStation(form) } returns station.right()
//            every { KeycloakGroupIntegration.createGroup(station.name, station.id) } returns expected.left()
//
//            val actual = StationService.saveStation(form)
//            require(actual is Either.Left)
//            assertEquals(expected, actual.a)
//        }
//    }
//
//    @Nested
//    inner class Delete {
//
//        /**
//         * A successfull deletion should return the deleted station
//         */
//        @Test
//        fun `delete station success`(@MockK(relaxed = true) expected: Station) {
//
//            every { StationRepository.deleteStation(1) } returns 1.right()
//            every { StationRepository.getStationById(1) } returns expected.right()
//            every { KeycloakGroupIntegration.deleteGroup(expected.name) } returns 1.right()
//
//            val actual = StationService.deleteStationById(1)
//            require(actual is Either.Right)
//            assertEquals(expected, actual.b)
//        }
//
//        /**
//         * Failing to delete a station in the database should return a repository error
//         */
//        @Test
//        fun `delete station repository error`(
//            @MockK(relaxed = true) station: Station,
//            @MockK expected: RepositoryError.DeleteError
//        ) {
//            every { StationRepository.getStationById(1) } returns station.right()
//            every { StationRepository.deleteStation(1) } returns expected.left()
//
//            val actual = StationService.deleteStationById(1)
//            require(actual is Either.Left)
//            assertEquals(expected, actual.a)
//        }
//
//        /**
//         * Failing to delete a station in keycloak should return a KeycloakIntegrationError.KeycloakError
//         */
//        @Test
//        fun `delete station keycloak error`(
//            @MockK(relaxed = true) station: Station,
//            @MockK expected: KeycloakIntegrationError.KeycloakError
//        ) {
//            every { StationRepository.getStationById(1) } returns station.right()
//            every { StationRepository.deleteStation(station.id) } returns 1.right()
//            every { KeycloakGroupIntegration.deleteGroup(station.name) } returns expected.left()
//
//            val actual = StationService.deleteStationById(1)
//            require(actual is Either.Left)
//            assertEquals(expected, actual.a)
//        }
//    }
//
//    @Nested
//    inner class Update {
//
//        /**
//         * A successful update should return the new station
//         */
//        @Test
//        fun `update station success`(
//            @MockK(relaxed = true) initial: Station,
//            @MockK(relaxed = true) form: StationUpdateForm,
//            @MockK(relaxed = true) expected: Station
//        ) {
//            every { StationRepository.getStationById(form.id) } returns initial.right()
//            every { StationRepository.updateStation(form) } returns expected.right()
//            every { KeycloakGroupIntegration.updateGroup(initial.name, expected.name) } returns Unit.right()
//
//            val actual = StationService.updateStation(form)
//            require(actual is Either.Right)
//            assertEquals(expected, actual.b)
//        }
//
//        /**
//         * When the station cannot be updated in the database, a RepositoryError.UpdateError should be returned
//         */
//        @Test
//        fun `update station repository error`(
//            @MockK(relaxed = true) initial: Station,
//            @MockK(relaxed = true) form: StationUpdateForm,
//            @MockK expected: RepositoryError.UpdateError
//        ) {
//            every { StationRepository.getStationById(form.id) } returns initial.right()
//            every { StationRepository.updateStation(form) } returns expected.left()
//
//            val actual = StationService.updateStation(form)
//            require(actual is Either.Left)
//            assertEquals(expected, actual.a)
//        }
//
//        /**
//         * When the station cannot be updated in keycloak, a KeycloakIntegrationError.KeycloakError should be returned
//         */
//        @Test
//        fun `update station keycloak error`(
//            @MockK(relaxed = true) initial: Station,
//            @MockK(relaxed = true) updated: Station,
//            @MockK(relaxed = true) form: StationUpdateForm,
//            @MockK expected: KeycloakIntegrationError.KeycloakError
//        ) {
//            every { StationRepository.getStationById(form.id) } returns initial.right()
//            every { StationRepository.updateStation(form) } returns updated.right()
//            every { KeycloakGroupIntegration.updateGroup(initial.name, updated.name) } returns expected.left()
//
//            val actual = StationService.updateStation(form)
//            require(actual is Either.Left)
//            assertEquals(expected, actual.a)
//        }
//    }
//}