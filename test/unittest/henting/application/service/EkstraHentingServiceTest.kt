package henting.application.service

import io.mockk.junit5.MockKExtension
import io.mockk.mockkClass
import ombruk.backend.henting.application.service.EkstraHentingService
import ombruk.backend.henting.infrastructure.repository.EkstraHentingRepository
import ombruk.backend.kategori.application.service.EkstraHentingKategoriService
import ombruk.backend.kategori.domain.entity.EkstraHentingKategori
import ombruk.backend.utlysning.application.service.UtlysningService
import ombruk.backend.vektregistrering.application.service.VektregistreringService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import testutils.mockDatabase
import testutils.unmockDatabase

@ExtendWith(MockKExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class EkstraHentingServiceTest {

    private lateinit var ekstraHentingService: EkstraHentingService
    private var utlysningService = mockkClass(UtlysningService::class)
    private var ekstraHentingRepository = mockkClass(EkstraHentingRepository::class)
    private var ekstraHentingKategoriService = mockkClass(EkstraHentingKategoriService::class)
    private var vektregistreringService = mockkClass(VektregistreringService::class)

    @BeforeEach
    fun setUp() {
        mockDatabase()
        ekstraHentingService = EkstraHentingService(ekstraHentingRepository, utlysningService, ekstraHentingKategoriService, vektregistreringService)
    }

    @AfterEach
    fun tearDown() {
        unmockDatabase()
    }
}