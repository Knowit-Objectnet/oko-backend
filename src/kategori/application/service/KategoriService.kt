package ombruk.backend.kategori.application.service

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ombruk.backend.kategori.application.api.dto.KategoriDeleteDto
import ombruk.backend.kategori.application.api.dto.KategoriFindDto
import ombruk.backend.kategori.application.api.dto.KategoriSaveDto
import ombruk.backend.kategori.domain.entity.Kategori
import ombruk.backend.kategori.domain.port.IKategoriRepository
import ombruk.backend.shared.error.ServiceError
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class KategoriService(val kategoriRepository: IKategoriRepository) : IKategoriService {
    override fun save(dto: KategoriSaveDto): Either<ServiceError, Kategori> {
        return transaction {
            kategoriRepository.insert(dto)
                .fold({ rollback(); it.left() }, { it.right() })
        }
    }

    override fun findOne(id: UUID): Either<ServiceError, Kategori> {
        return transaction {
            kategoriRepository.findOne(id)
        }
    }

    override fun find(dto: KategoriFindDto): Either<ServiceError, List<Kategori>> {
        return transaction {
            val kategorier = kategoriRepository.find(dto)
            kategorier.fold(
                { Either.Left(ServiceError(it.message)) },
                { it.right() }
            )
        }
    }

    override fun delete(dto: KategoriDeleteDto): Either<ServiceError, Unit> {
        return transaction {
            kategoriRepository.delete(dto.id)
                .fold({ rollback(); it.left() }, { it.right() })
        }
    }

}