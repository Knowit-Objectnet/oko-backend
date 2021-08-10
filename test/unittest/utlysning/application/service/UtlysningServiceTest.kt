package utlysning.application.service

import arrow.core.Either
import arrow.core.right
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkClass
import ombruk.backend.aarsak.aarsakModule
import ombruk.backend.aktor.application.service.KontaktService
import ombruk.backend.aktor.domain.entity.Kontakt
import ombruk.backend.avtale.avtaleModule
import ombruk.backend.henting.application.service.EkstraHentingService
import ombruk.backend.henting.application.service.IEkstraHentingService
import ombruk.backend.henting.domain.entity.EkstraHenting
import ombruk.backend.henting.hentingModule
import ombruk.backend.kategori.kategoriModule
import ombruk.backend.notification.application.service.NotificationService
import ombruk.backend.notification.domain.entity.Notification
import ombruk.backend.utlysning.application.api.dto.UtlysningBatchSaveDto
import ombruk.backend.utlysning.application.service.UtlysningService
import ombruk.backend.utlysning.domain.entity.Utlysning
import ombruk.backend.utlysning.infrastructure.repository.UtlysningRepository
import ombruk.backend.utlysning.utlysningModule
import ombruk.backend.vektregistrering.vektregistreringModule
import org.junit.jupiter.api.*

import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.get
import org.testcontainers.junit.jupiter.Testcontainers
import testutils.MockAktorModule
import testutils.TestContainer
import testutils.mockDatabase
import testutils.unmockDatabase
import java.time.LocalDateTime
import java.util.*
import kotlin.math.exp

@ExtendWith(MockKExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers
internal class UtlysningServiceTest: KoinTest {

    private val testContainer = TestContainer()

    private lateinit var utlysningService: UtlysningService
    private var utlysningRepository = mockkClass(UtlysningRepository::class)
    private var notificationService = mockkClass(NotificationService::class)
    private var kontaktService = mockkClass(KontaktService::class)
    private var ekstrahentingService: IEkstraHentingService = mockkClass(EkstraHentingService::class)

    @BeforeEach
    fun setUp() {
        testContainer.start()
        mockDatabase()

        startKoin {  }
        loadKoinModules(listOf(hentingModule, MockAktorModule.get(), utlysningModule, avtaleModule, kategoriModule, vektregistreringModule, aarsakModule))
        utlysningService = UtlysningService(utlysningRepository, notificationService, kontaktService)
    }

    @AfterEach
    fun tearDown() {
        testContainer.stop()
        unmockDatabase()
        stopKoin()
    }

    @Test
    fun batchCreate(@MockK expected: Utlysning) {
        val dto = UtlysningBatchSaveDto(
            hentingId = UUID.randomUUID(),
            partnerIds = listOf(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString())
        )

        val testHentingId = UUID.randomUUID()
        val testPartnerId = UUID.randomUUID()

        every { utlysningRepository.insert(any()) } returns expected.right()
        every { utlysningRepository.find(any()) } returns emptyList<Utlysning>().right()
        every { notificationService.sendMessage(any(), any(), emptyList<Kontakt>()) } returns Notification().right()
        every { kontaktService.getKontakter(any()) } returns emptyList<Kontakt>().right()
        every { expected.partnerId } returns testHentingId
        every { expected.hentingId } returns testPartnerId
        every { ekstrahentingService.findOne(any()) } returns EkstraHenting(UUID.randomUUID(), LocalDateTime.now(), LocalDateTime.now(),"", UUID.randomUUID(), "", utlysninger = listOf(expected)).right()

        val actualList = utlysningService.batchSave(dto)
        println(actualList)
        require(actualList is Either.Right)
        assert(actualList.b.size == 3)
        assert(actualList.b.all { it == expected })

    }
}