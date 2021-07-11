package notification

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkClass
import ombruk.backend.aktor.application.service.IVerifiseringService
import ombruk.backend.aktor.application.service.VerifiseringService
import ombruk.backend.aktor.domain.entity.Kontakt
import ombruk.backend.notification.application.service.NotificationService
import ombruk.backend.notification.application.service.SESService
import ombruk.backend.notification.application.service.SNSService
import ombruk.backend.notification.domain.entity.Notification
import ombruk.backend.notification.domain.entity.SES
import ombruk.backend.notification.domain.entity.SNS
import ombruk.backend.shared.error.ServiceError
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*

@ExtendWith(MockKExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class NotificationServiceTest {

    private lateinit var notificationService: NotificationService
    private var snsService = mockkClass(SNSService::class)
    private var sesService = mockkClass(SESService::class)
    private var verifiseringService = mockkClass(VerifiseringService::class)

    @BeforeEach
    fun setUp() {
        notificationService = NotificationService(snsService, sesService, verifiseringService)
    }

    @AfterEach
    fun tearDown() {}

    @Test
    fun sendMessageShouldSucceedTest() {
        every { snsService.sendMessage(any(), any()) } returns SNS(statusCode = 200)
        every { sesService.sendMessage(any(), any()) } returns SES(statusCode = 200)

        val kontakter = listOf<Kontakt>(
            Kontakt(
                id = UUID.fromString("e70e66e6-a063-42ef-826c-80d59a8b7f92"),
                aktorId = UUID.fromString("e70e66e6-a063-42ef-826c-80d59a8b7f92"),
                navn = "Test1",
                telefon = "+4790909090",
                epost = "test@knowit.no"
            )
        )

        val actual = notificationService.sendMessage("Test", kontakter)
        assert(actual is Either.Right<Notification>)
    }

    @Test
    fun sendMessageShouldFailTest() {
        every { snsService.sendMessage(any(), any()) } returns SNS(statusCode = 500)
        every { sesService.sendMessage(any(), any()) } returns SES(statusCode = 500)

        val actual = notificationService.sendMessage("Test", emptyList())
        assert(actual is Either.Left<ServiceError>)
    }
}