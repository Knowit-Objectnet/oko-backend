package ombruk.backend.vektregistrering

import ombruk.backend.kategori.application.service.HenteplanKategoriService
import ombruk.backend.kategori.application.service.IHenteplanKategoriService
import ombruk.backend.kategori.application.service.IKategoriService
import ombruk.backend.kategori.application.service.KategoriService
import ombruk.backend.kategori.domain.port.IHenteplanKategoriRepository
import ombruk.backend.kategori.domain.port.IKategoriRepository
import ombruk.backend.kategori.infrastructure.repository.HenteplanKategoriRepository
import ombruk.backend.kategori.infrastructure.repository.KategoriRepository
import ombruk.backend.vektregistrering.application.service.IVektregistreringService
import ombruk.backend.vektregistrering.application.service.VektregistreringService
import ombruk.backend.vektregistrering.domain.port.IVektregistreringRepository
import ombruk.backend.vektregistrering.infrastructure.repository.VektregistreringRepository
import org.koin.dsl.module

val vektregistreringModule = module(createdAtStart = true) {
    single<IVektregistreringRepository> { VektregistreringRepository() }
    single<IVektregistreringService> { VektregistreringService(get()) }
}