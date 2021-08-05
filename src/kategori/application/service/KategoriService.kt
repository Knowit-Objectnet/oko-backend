package ombruk.backend.kategori.application.service

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import ombruk.backend.kategori.application.api.dto.*
import ombruk.backend.kategori.domain.entity.Kategori
import ombruk.backend.kategori.domain.port.IKategoriRepository
import ombruk.backend.shared.error.ServiceError
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class KategoriService(
    val kategoriRepository: IKategoriRepository,
    val henteplanKategoriService: IHenteplanKategoriService
    ) : IKategoriService {
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

    override fun archiveOne(id: UUID): Either<ServiceError, Unit> {
        return transaction {
            kategoriRepository.archiveOne(id)
                .map { kategori ->
                    henteplanKategoriService.archive(HenteplanKategoriFindDto(kategoriId = kategori.id))
                }.flatMap { it }
                .fold({rollback(); it.left()}, {it.right()})
        }
    }

    override fun update(dto: KategoriUpdateDto): Either<ServiceError, Kategori> {
        return transaction {
            if (dto.id == UUID.fromString("0f3f3bdd-5733-45da-87ae-a9417596cb12")) ServiceError("Illegal category to update").left()
            else kategoriRepository.update(dto)
        }
    }
}