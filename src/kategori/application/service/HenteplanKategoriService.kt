package ombruk.backend.kategori.application.service

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ombruk.backend.kategori.application.api.dto.*
import ombruk.backend.kategori.domain.entity.HenteplanKategori
import ombruk.backend.kategori.domain.port.IHenteplanKategoriRepository
import ombruk.backend.shared.error.ServiceError
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class HenteplanKategoriService(val henteplanKategoriRepository: IHenteplanKategoriRepository) : IHenteplanKategoriService {
    override fun save(dto: HenteplanKategoriSaveDto): Either<ServiceError, HenteplanKategori> {
        return transaction {
            henteplanKategoriRepository.insert(dto)
                .fold({ rollback(); it.left() }, { it.right() })
        }
    }

    override fun findOne(id: UUID): Either<ServiceError, HenteplanKategori> {
        return transaction {
            henteplanKategoriRepository.findOne(id)
        }
    }

    override fun find(dto: HenteplanKategoriFindDto): Either<ServiceError, List<HenteplanKategori>> {
        return transaction {
            henteplanKategoriRepository.find(dto)
        }
    }

    override fun delete(dto: HenteplanKategoriDeleteDto): Either<ServiceError, Unit> {
        return transaction {
            henteplanKategoriRepository.delete(dto.id)
                .fold({ rollback(); it.left() }, { it.right() })
        }
    }

}