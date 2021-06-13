package ombruk.backend.kategori.application.service

import arrow.core.Either
import arrow.core.extensions.either.applicative.applicative
import arrow.core.extensions.list.traverse.sequence
import arrow.core.fix
import arrow.core.left
import arrow.core.right
import ombruk.backend.kategori.application.api.dto.*
import ombruk.backend.kategori.domain.entity.HenteplanKategori
import ombruk.backend.kategori.domain.port.IHenteplanKategoriRepository
import ombruk.backend.kategori.domain.port.IKategoriRepository
import ombruk.backend.shared.error.RepositoryError
import ombruk.backend.shared.error.ServiceError
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class HenteplanKategoriService(val henteplanKategoriRepository: IHenteplanKategoriRepository, val kategoriService: IKategoriService) : IHenteplanKategoriService {
    override fun save(dto: HenteplanKategoriSaveDto): Either<ServiceError, HenteplanKategori> {
        return transaction {
            henteplanKategoriRepository.insert(dto)
                .fold({ rollback(); it.left() }, { it.right() })
        }
    }

    override fun findOne(id: UUID): Either<ServiceError, HenteplanKategori> {
        /*return transaction {
            kategoriRepository.findOne(id)
        }*/
        return Either.left(RepositoryError.SelectError("no need for findOne here..."))
    }

    override fun find(dto: HenteplanKategoriFindDto): Either<ServiceError, List<HenteplanKategori>> {
        return transaction {
            val henteplanKategorier = henteplanKategoriRepository.find(dto)
            henteplanKategorier.fold(
                { Either.Left(ServiceError(it.message)) },
                {
                    it.map { henteplanKategori ->
                        kategoriService.findOne(henteplanKategori.kategoriId).fold(
                            { henteplanKategori.right() },
                            { henteplanKategori.copy(kategori = it).right() }
                        )
                    }.sequence(Either.applicative()).fix().map { it.fix() }
                }
            )
        }
    }

    override fun delete(dto: HenteplanKategoriDeleteDto): Either<ServiceError, Unit> {
        return transaction {
            henteplanKategoriRepository.delete(dto.id)
                .fold({ rollback(); it.left() }, { it.right() })
        }
    }

}