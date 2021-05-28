package ombruk.backend.henting.application.service

import arrow.core.*
import arrow.core.extensions.either.applicative.applicative
import arrow.core.extensions.list.traverse.sequence
import henting.application.api.dto.HenteplanInsertDto
import io.ktor.locations.*
import ombruk.backend.henting.application.api.dto.*
import ombruk.backend.henting.domain.entity.Henteplan
import ombruk.backend.henting.domain.entity.PlanlagtHenting
import ombruk.backend.henting.domain.port.IHenteplanRepository
import ombruk.backend.shared.error.ServiceError
import ombruk.backend.shared.utils.LocalDateTimeProgressionWithDayFrekvens
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

@KtorExperimentalLocationsAPI
class HenteplanService(val henteplanRepository: IHenteplanRepository, val planlagtHentingService: IPlanlagtHentingService) : IHenteplanService {

    fun createPlanlagtHentinger(dto: HenteplanInsertDto, henteplanId: UUID): Either<ServiceError, List<PlanlagtHenting>> {
        //Find all dates
        val dates = LocalDateTimeProgressionWithDayFrekvens(dto.startTidspunkt, dto.sluttTidspunkt, dto.ukedag, dto.frekvens)
            .map { it.toLocalDate() }
        val postDto = PlanlagtHentingInsertDto(dto.startTidspunkt, dto.sluttTidspunkt, null, henteplanId)
        return planlagtHentingService.batchCreateForHenteplan(PlanlagtHentingBatchPostDto(postDto, dates))
    }

    fun appendPlanlagtHentinger(dto: HenteplanInsertDto, id: UUID, henteplan: Henteplan) =
        run {
            val hentinger: Either<ServiceError, List<PlanlagtHenting>> = createPlanlagtHentinger(dto, id)
            when (hentinger) {
                is Either.Left -> hentinger
                is Either.Right -> henteplan.copy(planlagteHentinger = hentinger.b).right()
            }
        }

    override fun create(dto: HenteplanInsertDto): Either<ServiceError, Henteplan> {

        val henteplan = transaction {
            henteplanRepository.insert(dto)
                .fold(
                    { Either.Left(ServiceError(it.message)) },
                    { appendPlanlagtHentinger(dto, it.id, it) }
                )
                .fold({rollback(); it.left()}, {it.right()})
        }
        return henteplan

    }

    override fun batchCreate(dto: List<HenteplanInsertDto>): Either<ServiceError, List<Henteplan>> {
        return transaction {
            dto.map {curDto -> henteplanRepository.insert(curDto)
                .fold(
                    {Either.Left(ServiceError(it.message))},
                    { appendPlanlagtHentinger(curDto, it.id, it) }
                )}
                .sequence(Either.applicative())
                .fix()
                .map { it.fix() }
            .fold({rollback(); it.left()}, {it.right()})
        }
    }

    //TODO: Create find calls including planlagteHentinger

    override fun findOne(id: UUID): Either<ServiceError, Henteplan> {
        return transaction { henteplanRepository.findOne(id) }
    }

    override fun find(dto: HenteplanFindDto): Either<ServiceError, List<Henteplan>> {
        return transaction { henteplanRepository.find(dto) }
    }

    override fun findAllForAvtale(avtaleId: UUID): Either<ServiceError, List<Henteplan>> {
        return find(HenteplanFindDto(avtaleId = avtaleId))
    }

    override fun delete(dto: HenteplanDeleteDto): Either<ServiceError, Unit> {
        return transaction { henteplanRepository.delete(dto.id) }
    }

    override fun update(dto: HenteplanUpdateDto): Either<ServiceError, Henteplan> {
        // TODO: Add planlagt henting update logic
        return transaction { henteplanRepository.update(dto) }
    }
}