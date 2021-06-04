package ombruk.backend.avtale.application.service

import arrow.core.*
import arrow.core.extensions.either.applicative.applicative
import arrow.core.extensions.list.traverse.sequence
import avtale.application.api.dto.AvtaleSaveDto
import ombruk.backend.avtale.application.api.dto.AvtaleDeleteDto
import ombruk.backend.avtale.application.api.dto.AvtaleFindDto
import ombruk.backend.avtale.domain.entity.Avtale
import ombruk.backend.avtale.domain.port.IAvtaleRepository
import ombruk.backend.henting.application.service.IHenteplanService
import ombruk.backend.shared.error.ServiceError
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class AvtaleService(val avtaleRepository: IAvtaleRepository, val hentePlanService: IHenteplanService) : IAvtaleService {
    override fun save(dto: AvtaleSaveDto): Either<ServiceError, Avtale> {
        return transaction {
            avtaleRepository.insert(dto)
                .flatMap { avtale ->
                    if(dto.henteplaner != null) {
                        return@flatMap hentePlanService.batchSave(dto.henteplaner.map { it.copy(avtaleId = avtale.id) })
                            .fold({ it.left() }, { avtale.copy(henteplaner = it).right() })
                    } else {
                        return@flatMap avtale.right()
                    }
                }
                .fold({rollback(); it.left()}, {it.right()})
        }
    }

    override fun findOne(id: UUID): Either<ServiceError, Avtale> {
        return transaction {
            avtaleRepository.findOne(id)
                .flatMap { avtale ->
                    hentePlanService.findAllForAvtale(avtale.id)
                        .flatMap { planer -> avtale.copy(henteplaner = planer).right() }
                }
        }
    }

    override fun find(dto: AvtaleFindDto): Either<ServiceError, List<Avtale>> {

        return transaction {

            val avtaler = avtaleRepository.find(dto)
            avtaler.fold(
                {Either.Left(ServiceError(it.message))},
                {
                    it.map { avtale ->
                        hentePlanService.findAllForAvtale(avtale.id)
                            .fold({ it.left() }, { avtale.copy(henteplaner = it).right() })
                    }.sequence(Either.applicative()).fix().map { it.fix() }
                }
            )

        }
    }

    override fun delete(dto: AvtaleDeleteDto): Either<ServiceError, Unit> {
        return transaction {
            avtaleRepository.delete(dto.id)
                .fold({rollback(); it.left()}, {it.right()})
        }
    }

}