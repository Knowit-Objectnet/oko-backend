package utlysning.application.service

import arrow.core.Either
import arrow.core.right
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkClass
import ombruk.backend.utlysning.application.api.dto.UtlysningBatchPostDto
import ombruk.backend.utlysning.application.service.UtlysningService
import ombruk.backend.utlysning.domain.entity.Utlysning
import ombruk.backend.utlysning.infrastructure.repository.UtlysningRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
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

    @BeforeEach
    fun setUp() {
        mockDatabase()
        utlysningService = UtlysningService(utlysningRepository)
    }

    @AfterEach
    fun tearDown() {
        unmockDatabase()
    }

    @Test
    fun batchCreate(@MockK expected: Utlysning) {
        val dto = UtlysningBatchPostDto(
            hentingId = UUID.randomUUID(),
            partnerIds = listOf(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
        )

        every { utlysningRepository.insert(any()) } returns expected.right()

        val actualList = utlysningService.batchCreate(dto)
        require(actualList is Either.Right)
        assert(actualList.b.size == 3)
        assert(actualList.b.all { it == expected })
    }
}