package notification

import io.mockk.*
import io.mockk.junit5.MockKExtension
import ombruk.backend.notification.application.service.SNSService
import ombruk.backend.notification.domain.entity.SNS
import ombruk.backend.notification.domain.params.SESInputParams
import ombruk.backend.notification.domain.params.SNSInputParams
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class SNSServiceTest {

    private lateinit var snsService: SNSService

    @BeforeEach
    fun setUp() {
        snsService = mockk<SNSService> {
            every { sendMessage(any(), any()) } answers { SNS(200, "test") }
        }
    }

    @AfterEach
    fun tearDown() {}

    @Test
    fun sendMessageShouldSucceedTest() {
        val kontakter = listOf<String>(
            "40404040"
        )

        val snsinputparams: SNSInputParams = SNSInputParams("SMSTestSubject", "SMSTestMessage")
        val actual = snsService.sendMessage(snsinputparams, kontakter)
        assertEquals(200, actual.statusCode)
        assertEquals("test", actual.message)
    }
}