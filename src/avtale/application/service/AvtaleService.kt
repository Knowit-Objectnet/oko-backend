package ombruk.backend.avtale.application.service

import arrow.core.Either
import arrow.core.extensions.either.monad.flatMap
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import avtale.application.api.dto.AvtalePostDto
import ombruk.backend.avtale.application.api.dto.AvtaleDeleteDto
import ombruk.backend.avtale.application.api.dto.AvtaleFindDto
import ombruk.backend.avtale.domain.entity.Avtale
import ombruk.backend.avtale.domain.port.IAvtaleRepository
import ombruk.backend.henting.application.api.dto.HenteplanFindDto
import ombruk.backend.henting.application.service.IHenteplanService
import ombruk.backend.shared.error.ServiceError
import org.jetbrains.exposed.sql.transactions.transaction

class AvtaleService(val avtaleRepository: IAvtaleRepository, val hentePlanService: IHenteplanService) : IAvtaleService {
    override fun save(dto: AvtalePostDto): Either<ServiceError, Avtale> {
        return transaction {
            avtaleRepository.insert(dto)
                .flatMap { avtale ->
                    hentePlanService.batchCreate(dto.henteplaner.map { it.copy(avtaleId = avtale.id) })
                        .fold({ it.left() }, { avtale.copy(henteplaner = it).right() })
                }
        }
    }

    override fun findOne(id: Int): Either<ServiceError, Avtale> {
        return avtaleRepository.findOne(id)
            .flatMap { avtale ->
                hentePlanService.find(HenteplanFindDto(avtaleId = avtale.id))
                    .flatMap { planer -> avtale.copy(henteplaner = planer).right() }
            }
    }

    override fun find(dto: AvtaleFindDto): Either<ServiceError, List<Avtale>> {
        val avtaler = avtaleRepository.find(dto)
        if (avtaler.isLeft()) {
            return avtaler
        }
//        val avtaleIds = avtaler.map { it.id }
        TODO("Not yet implemented")
    }

    override fun delete(dto: AvtaleDeleteDto): Either<ServiceError, Unit> {
        return transaction {
            avtaleRepository.delete(dto.id)
        }
    }
}