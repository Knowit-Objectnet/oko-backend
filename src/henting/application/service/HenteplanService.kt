package ombruk.backend.henting.application.service

import arrow.core.*
import arrow.core.extensions.either.applicative.applicative
import arrow.core.extensions.list.traverse.sequence
import arrow.core.extensions.sequence.apply.map
import henting.application.api.dto.HenteplanSaveDto
import io.ktor.locations.*
import ombruk.backend.henting.application.api.dto.*
import ombruk.backend.henting.domain.entity.Henteplan
import ombruk.backend.henting.domain.entity.PlanlagtHentingWithParents
import ombruk.backend.henting.domain.model.HenteplanFrekvens
import ombruk.backend.henting.domain.params.HenteplanFindParams
import ombruk.backend.henting.domain.port.IHenteplanRepository
import ombruk.backend.kategori.application.api.dto.HenteplanKategoriFindDto
import ombruk.backend.kategori.application.service.IHenteplanKategoriService
import ombruk.backend.shared.error.ServiceError
import ombruk.backend.shared.utils.LocalDateTimeProgressionWithDayFrekvens
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.util.*

@KtorExperimentalLocationsAPI
class HenteplanService(val henteplanRepository: IHenteplanRepository, val planlagtHentingService: IPlanlagtHentingService, val henteplanKategoriService: IHenteplanKategoriService) : IHenteplanService {

    fun createPlanlagtHentinger(dto: HenteplanSaveDto, henteplanId: UUID): Either<ServiceError, List<PlanlagtHentingWithParents>> {
        //Find all dates
        val dates = LocalDateTimeProgressionWithDayFrekvens(dto.startTidspunkt, dto.sluttTidspunkt, dto.ukedag, dto.frekvens)
            .map { it.toLocalDate() }
        val postDto = PlanlagtHentingSaveDto(dto.startTidspunkt, dto.sluttTidspunkt, null, henteplanId)
        return planlagtHentingService.batchSaveForHenteplan(PlanlagtHentingBatchPostDto(postDto, dates))
    }

    fun appendPlanlagtHentinger(dto: HenteplanSaveDto, id: UUID, henteplan: Henteplan): Either<ServiceError, Henteplan> =
        run {
            val hentinger: Either<ServiceError, List<PlanlagtHentingWithParents>> = createPlanlagtHentinger(dto, id)
            when (hentinger) {
                is Either.Left -> hentinger
                is Either.Right -> henteplan.copy(planlagteHentinger = hentinger.b).right()
            }
        }

    override fun save(dto: HenteplanSaveDto): Either<ServiceError, Henteplan> {

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

    override fun batchSave(dto: List<HenteplanSaveDto>): Either<ServiceError, List<Henteplan>> {
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
        return transaction {
            henteplanRepository.find(dto)
                .fold(
                    { Either.Left(ServiceError(it.message)) },
                    {
                        it.map { henteplan ->
                            henteplanKategoriService.find(HenteplanKategoriFindDto(henteplanId = henteplan.id))
                                .fold(
                                    { henteplan.right() },
                                    { henteplan.copy(kategorier = it).right() }
                                )
                        }.sequence(Either.applicative()).fix().map { it.fix() }
                    }
                )
        }
    }

    override fun findAllForAvtale(avtaleId: UUID): Either<ServiceError, List<Henteplan>> {
        return find(HenteplanFindDto(avtaleId = avtaleId))
    }

    override fun delete(dto: HenteplanDeleteDto): Either<ServiceError, Unit> {
        return transaction { henteplanRepository.delete(dto.id) }
    }

    override fun update(dto: HenteplanUpdateDto): Either<ServiceError, Henteplan> {
        var today = LocalDateTime.now()
        // Funksjonen sletter først og deretter legger til nye planlagte hentinger
            findOne(dto.id).fold({}, {
                planlagtHentingService.find(PlanlagtHentingFindDto(henteplanId = dto.id, after = today)).map {
                    it.map { planlagtHentingService.delete(PlanlagtHentingDeleteDto(id = it.id)) }
                }

                // Legger til "planlagte hentinger"
                val starttime = LocalDateTime.of(today.toLocalDate(), (dto.startTidspunkt ?: it.startTidspunkt).toLocalTime())
                    transaction {
                        appendPlanlagtHentinger(
                            HenteplanSaveDto(
                                avtaleId = it.avtaleId,
                                stasjonId = it.stasjonId,
                                startTidspunkt = starttime,
                                sluttTidspunkt = dto.sluttTidspunkt ?: it.sluttTidspunkt,
                                ukedag = dto.ukeDag ?: it.ukedag,
                                merknad = it.merknad,
                                frekvens = dto.frekvens ?: it.frekvens
                            ), it.id, it
                        ).fold({ rollback() }, {})}

            })
        return transaction { henteplanRepository.update(dto) }
    }

    override fun archiveOne(id: UUID): Either<ServiceError, Unit> {
        return transaction {
            henteplanRepository.archiveOne(id)
                .fold(
                    {Either.Left(ServiceError(it.message))},
                    { planlagtHentingService.archive(PlanlagtHentingFindDto(henteplanId = it.id, after = LocalDateTime.now()))}
                )
                .fold({rollback(); it.left()}, {it.right()})
        }
    }

    override fun archive(params: HenteplanFindParams): Either<ServiceError, Unit> {
        return transaction {
            henteplanRepository.archive(params)
                .fold(
                    {Either.Left(ServiceError(it.message))},
                    { henteplan ->
                        henteplan.map { planlagtHentingService.archive(PlanlagtHentingFindDto(henteplanId = it.id, after = LocalDateTime.now())) }
                            .sequence(Either.applicative())
                            .fix()
                            .map { it.fix() }
                            .flatMap { Either.right(Unit) }
                    }
                )
                .fold({rollback(); it.left()}, {it.right()})
        }
    }

}