package ombruk.backend.henting.application.service

import arrow.core.Either
import arrow.core.extensions.either.applicative.applicative
import arrow.core.extensions.list.traverse.sequence
import arrow.core.fix
import arrow.core.left
import arrow.core.right
import henting.application.api.dto.HenteplanPostDto
import io.ktor.locations.*
import ombruk.backend.henting.application.api.dto.HenteplanDeleteDto
import ombruk.backend.henting.application.api.dto.HenteplanFindDto
import ombruk.backend.henting.application.api.dto.HenteplanUpdateDto
import ombruk.backend.henting.domain.entity.Henteplan
import ombruk.backend.henting.domain.port.IHenteplanRepository
import ombruk.backend.shared.error.ServiceError
import org.jetbrains.exposed.sql.transactions.transaction

@KtorExperimentalLocationsAPI
class HenteplanService(val henteplanRepository: IHenteplanRepository) : IHenteplanService {
    override fun create(dto: HenteplanPostDto): Either<ServiceError, Henteplan> {
        if(dto.avtaleId == null) {
            return ServiceError(message = "avtaleId cannot be null").left()
        }
        return henteplanRepository.insert(dto)
        // TODO: Add planlagt henting insertion logic
    }

    override fun batchCreate(dto: List<HenteplanPostDto>): Either<ServiceError, List<Henteplan>> {
        if(dto.any { it.avtaleId == null }) {
            return ServiceError(message = "avtaleId cannot be null").left()
        }
        // TODO: Add planlagt henting insertion logic
        return transaction {
            dto.map { henteplanRepository.insert(it) }
                .sequence(Either.applicative())
                .fix()
                .map { it.fix() }
            .fold({rollback(); it.left()}, {it.right()})
        }
    }

    override fun findOne(id: Int): Either<ServiceError, Henteplan> {
        return henteplanRepository.findOne(id)
    }

    override fun find(dto: HenteplanFindDto): Either<ServiceError, List<Henteplan>> {
        return henteplanRepository.find(dto)
    }

    override fun delete(dto: HenteplanDeleteDto): Either<ServiceError, Unit> {
        return henteplanRepository.delete(dto.id)
    }

    override fun update(dto: HenteplanUpdateDto): Either<ServiceError, Henteplan> {
        // TODO: Add planlagt henting update logic
        return henteplanRepository.update(dto)
    }
}