package utlysning.application.service

import arrow.core.Either
import arrow.core.right
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkClass
import ombruk.backend.aktor.application.service.KontaktService
import ombruk.backend.aktor.domain.entity.Kontakt
import ombruk.backend.notification.application.service.NotificationService
import ombruk.backend.notification.domain.entity.Notification
import ombruk.backend.utlysning.application.api.dto.UtlysningBatchSaveDto
import ombruk.backend.utlysning.application.service.UtlysningService
import ombruk.backend.utlysning.domain.entity.Utlysning
import ombruk.backend.utlysning.infrastructure.repository.UtlysningRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import testutils.mockDatabase
import testutils.unmockDatabase
import java.util.*

@ExtendWith(MockKExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class UtlysningServiceTest {

    private lateinit var utlysningService: UtlysningService
    private var utlysningRepository = mockkClass(UtlysningRepository::class)
    private var notificationService = mockkClass(NotificationService::class)
    private var kontaktService = mockkClass(KontaktService::class)

    @BeforeEach
    fun setUp() {
        mockDatabase()
        utlysningService = UtlysningService(utlysningRepository, notificationService, kontaktService)
    }

    @AfterEach
    fun tearDown() {
        unmockDatabase()
    }

    @Test
    fun batchCreate(@MockK expected: Utlysning) {
        val dto = UtlysningBatchSaveDto(
            hentingId = UUID.randomUUID(),
            partnerIds = listOf(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString())
        )

        every { utlysningRepository.insert(any()) } returns expected.right()
        every { utlysningRepository.find(any()) } returns emptyList<Utlysning>().right()
        every { notificationService.sendMessage(any(), any()) } returns Notification().right()
        every { kontaktService.getKontakter(any()) } returns emptyList<Kontakt>().right()
        every { expected.partnerId } returns UUID.randomUUID()

        val actualList = utlysningService.batchSave(dto)
        println(actualList)
        require(actualList is Either.Right)
        assert(actualList.b.size == 3)
        assert(actualList.b.all { it == expected })
    }
}