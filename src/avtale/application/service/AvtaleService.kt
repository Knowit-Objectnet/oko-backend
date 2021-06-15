package ombruk.backend.avtale.application.service

import arrow.core.*
import arrow.core.extensions.either.applicative.applicative
import arrow.core.extensions.either.foldable.fold
import arrow.core.extensions.either.foldable.foldRight
import arrow.core.extensions.either.foldable.get
import arrow.core.extensions.list.traverse.sequence
import avtale.application.api.dto.AvtaleSaveDto
import ombruk.backend.avtale.application.api.dto.AvtaleDeleteDto
import ombruk.backend.avtale.application.api.dto.AvtaleFindDto
import ombruk.backend.avtale.application.api.dto.AvtaleUpdateDto
import ombruk.backend.avtale.domain.entity.Avtale
import ombruk.backend.avtale.domain.port.IAvtaleRepository
import ombruk.backend.henting.application.api.dto.HenteplanFindDto
import ombruk.backend.henting.application.api.dto.HenteplanUpdateDto
import ombruk.backend.henting.application.api.dto.PlanlagtHentingFindDto
import ombruk.backend.henting.application.service.IHenteplanService
import ombruk.backend.henting.domain.entity.Henteplan
import ombruk.backend.shared.error.ServiceError
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.util.*

class AvtaleService(val avtaleRepository: IAvtaleRepository, val henteplanService: IHenteplanService) : IAvtaleService {
    override fun save(dto: AvtaleSaveDto): Either<ServiceError, Avtale> {
        return transaction {
            avtaleRepository.insert(dto)
                .flatMap { avtale ->
                    if (dto.henteplaner != null) {
                        return@flatMap henteplanService.batchSave(dto.henteplaner.map { it.copy(avtaleId = avtale.id) })
                            .fold({ it.left() }, { avtale.copy(henteplaner = it).right() })
                    } else {
                        return@flatMap avtale.right()
                    }
                }
                .fold({ rollback(); it.left() }, { it.right() })
        }
    }

    override fun findOne(id: UUID): Either<ServiceError, Avtale> {
        return transaction {
            avtaleRepository.findOne(id)
                .flatMap { avtale ->
                    henteplanService.findAllForAvtale(avtale.id)
                        .flatMap { planer -> avtale.copy(henteplaner = planer).right() }
                }
        }
    }

    override fun find(dto: AvtaleFindDto): Either<ServiceError, List<Avtale>> {
        return transaction {
            val avtaler = avtaleRepository.find(dto)
            avtaler.fold(
                { Either.Left(ServiceError(it.message)) },
                {
                    it.map { avtale ->
                        henteplanService.findAllForAvtale(avtale.id)
                            .fold({ it.left() }, { avtale.copy(henteplaner = it).right() })
                    }.sequence(Either.applicative()).fix().map { it.fix() }
                }
            )
        }
    }

    override fun delete(dto: AvtaleDeleteDto): Either<ServiceError, Unit> {
        return transaction {
            avtaleRepository.delete(dto.id)
                .fold({ rollback(); it.left() }, { it.right() })
        }
    }

    override fun update(dto: AvtaleUpdateDto): Either<ServiceError, Avtale> {
        findOne(dto.id).map {
            it.henteplaner.map {
                val start: LocalDateTime = LocalDateTime.of(dto.startDato, it.startTidspunkt.toLocalTime())
                val slutt: LocalDateTime = LocalDateTime.of(dto.sluttDato, it.sluttTidspunkt.toLocalTime())
                if (!start.isEqual(it.startTidspunkt) || !slutt.isEqual(it.sluttTidspunkt)) {
                    henteplanService.update(HenteplanUpdateDto(id = it.id, startTidspunkt = start, sluttTidspunkt = slutt))
                }
            }
        }
        return transaction {
            avtaleRepository.update(dto)
                .fold({ rollback(); it.left() }, { it.right() })
        }
    }

    override fun archiveOne(id: UUID): Either<ServiceError, Unit> {
        return transaction {
            avtaleRepository.archiveOne(id)
                .fold(
                    { Either.Left(ServiceError(it.message)) },
                    { henteplanService.archive(HenteplanFindDto(avtaleId = it.id)) }
                )
                .fold({ rollback(); it.left() }, { it.right() })
        }
    }
}

