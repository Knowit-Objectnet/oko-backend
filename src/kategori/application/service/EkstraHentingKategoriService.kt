package ombruk.backend.kategori.application.service

import arrow.core.Either
import arrow.core.extensions.either.applicative.applicative
import arrow.core.extensions.list.traverse.sequence
import arrow.core.fix
import arrow.core.left
import arrow.core.right
import ombruk.backend.kategori.application.api.dto.*
import ombruk.backend.kategori.domain.entity.EkstraHentingKategori
import ombruk.backend.kategori.domain.entity.HenteplanKategori
import ombruk.backend.kategori.domain.params.EkstraHentingKategoriFindParams
import ombruk.backend.kategori.domain.params.HenteplanKategoriFindParams
import ombruk.backend.kategori.domain.port.IEkstraHentingKategoriRepository
import ombruk.backend.kategori.domain.port.IHenteplanKategoriRepository
import ombruk.backend.kategori.domain.port.IKategoriRepository
import ombruk.backend.shared.error.RepositoryError
import ombruk.backend.shared.error.ServiceError
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

class EkstraHentingKategoriService(val ekstraHentingKategoriRepository: IEkstraHentingKategoriRepository) : IEkstraHentingKategoriService, KoinComponent {
    //Using inject rather than constructor to avoid circular dependency
    private val kategoriService: IKategoriService by inject()

    override fun save(dto: EkstraHentingKategoriSaveDto): Either<ServiceError, EkstraHentingKategori> {
        return transaction {
            ekstraHentingKategoriRepository.insert(dto)
                .fold({ rollback(); it.left() }, { it.right() })
        }
    }

    override fun findOne(id: UUID): Either<ServiceError, EkstraHentingKategori> {
        /*return transaction {
            kategoriRepository.findOne(id)
        }*/
        return Either.left(RepositoryError.SelectError("no need for findOne here..."))
    }

    override fun find(dto: EkstraHentingKategoriFindDto): Either<ServiceError, List<EkstraHentingKategori>> {
        return transaction {
            ekstraHentingKategoriRepository.find(dto)
                .fold(
                    { Either.Left(ServiceError(it.message)) },
                    {
                        it.map { ekstraHentingKategori ->
                            kategoriService.findOne(ekstraHentingKategori.kategoriId)
                                .fold(
                                    { ekstraHentingKategori.right() },
                                    { ekstraHentingKategori.copy(kategori = it).right() }
                                )
                        }.sequence(Either.applicative()).fix().map { it.fix() }
                    }
                )
        }
    }

    override fun delete(dto: EkstraHentingKategoriDeleteDto): Either<ServiceError, Unit> {
        return transaction {
            ekstraHentingKategoriRepository.delete(dto.id)
                .fold({ rollback(); it.left() }, { it.right() })
        }
    }

    override fun archive(params: EkstraHentingKategoriFindParams): Either<ServiceError, Unit> {
        return transaction {
            ekstraHentingKategoriRepository.archive(params).map {}
                .fold({ rollback(); it.left() }, { it.right() })
        }
    }

    override fun archiveOne(id: UUID): Either<ServiceError, Unit> {
        return transaction {
            ekstraHentingKategoriRepository.archiveOne(id).map {}
                .fold({ rollback(); it.left() }, { it.right() })
        }
    }
}