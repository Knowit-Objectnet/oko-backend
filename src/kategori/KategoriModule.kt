package ombruk.backend.kategori

import ombruk.backend.kategori.application.service.HenteplanKategoriService
import ombruk.backend.kategori.application.service.IHenteplanKategoriService
import ombruk.backend.kategori.application.service.IKategoriService
import ombruk.backend.kategori.application.service.KategoriService
import ombruk.backend.kategori.domain.port.IHenteplanKategoriRepository
import ombruk.backend.kategori.domain.port.IKategoriRepository
import ombruk.backend.kategori.infrastructure.repository.HenteplanKategoriRepository
import ombruk.backend.kategori.infrastructure.repository.KategoriRepository
import org.koin.dsl.module

val kategoriModule = module(createdAtStart = true) {
    single<IKategoriRepository> { KategoriRepository() }
    single<IKategoriService> { KategoriService(get(), get()) }
    single<IHenteplanKategoriRepository> { HenteplanKategoriRepository() }
    single<IHenteplanKategoriService> { HenteplanKategoriService(get(), get()) }
}