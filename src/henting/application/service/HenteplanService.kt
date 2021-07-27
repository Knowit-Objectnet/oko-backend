package ombruk.backend.henting.application.service

import arrow.core.*
import arrow.core.extensions.either.applicative.applicative
import arrow.core.extensions.list.traverse.sequence
import henting.application.api.dto.HenteplanSaveDto
import io.ktor.locations.*
import ombruk.backend.henting.application.api.dto.*
import ombruk.backend.henting.domain.entity.Henteplan
import ombruk.backend.henting.domain.entity.PlanlagtHenting
import ombruk.backend.henting.domain.params.HenteplanFindParams
import ombruk.backend.henting.domain.port.IHenteplanRepository
import ombruk.backend.kategori.application.api.dto.HenteplanKategoriBatchSaveDto
import ombruk.backend.kategori.application.api.dto.HenteplanKategoriDeleteDto
import ombruk.backend.kategori.application.api.dto.HenteplanKategoriFindDto
import ombruk.backend.kategori.application.api.dto.HenteplanKategoriSaveDto
import ombruk.backend.kategori.application.service.IHenteplanKategoriService
import ombruk.backend.shared.error.ServiceError
import ombruk.backend.shared.utils.LocalDateTimeProgressionWithDayFrekvens
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.util.*

@KtorExperimentalLocationsAPI
class HenteplanService(val henteplanRepository: IHenteplanRepository, val planlagtHentingService: IPlanlagtHentingService, val henteplanKategoriService: IHenteplanKategoriService) : IHenteplanService {

    fun createPlanlagtHentinger(dto: HenteplanSaveDto, henteplanId: UUID): Either<ServiceError, List<PlanlagtHenting>> {
        //Find all dates
        val dates = LocalDateTimeProgressionWithDayFrekvens(dto.startTidspunkt, dto.sluttTidspunkt, dto.ukedag, dto.frekvens)
            .map { it.toLocalDate() }
        val postDto = PlanlagtHentingSaveDto(dto.startTidspunkt, dto.sluttTidspunkt, henteplanId)
        return planlagtHentingService.batchSaveForHenteplan(PlanlagtHentingBatchPostDto(postDto, dates))
    }

    fun appendPlanlagtHentinger(dto: HenteplanSaveDto, id: UUID, henteplan: Henteplan): Either<ServiceError, Henteplan> =
        run {
            val hentinger: Either<ServiceError, List<PlanlagtHenting>> = createPlanlagtHentinger(dto, id)
            when (hentinger) {
                is Either.Left -> hentinger
                is Either.Right -> henteplan.copy(planlagteHentinger = hentinger.b).right()
            }
        }

    fun appendKategorier(dto: HenteplanSaveDto, id: UUID, henteplan: Henteplan): Either<ServiceError, Henteplan>
        = run {
            if (dto.kategorier == null) {return Either.Right(henteplan)}
            dto.kategorier!!.map {
                henteplanKategoriService.save(
                    HenteplanKategoriSaveDto(
                        henteplanId = id,
                        kategoriId = it.kategoriId
                    )
                )
            }
                .sequence(Either.applicative())
                .fix()
                .map { it.fix() }
                .fold({it.left()}, {henteplan.copy(kategorier = it).right()})
        }


    override fun save(dto: HenteplanSaveDto): Either<ServiceError, Henteplan> {

        return transaction {
            henteplanRepository.insert(dto)
                .fold(
                    { Either.Left(ServiceError(it.message)) },
                    {
                        appendPlanlagtHentinger(dto, it.id, it)
                            .flatMap {
                                appendKategorier(dto, it.id, it)
                            }
                    }
                )
                .fold({ rollback(); it.left() }, { it.right() })
        }

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
        return transaction {
            henteplanRepository.findOne(id)
                .fold(
                    { Either.Left(ServiceError(it.message)) },
                    { henteplan ->
                    henteplanKategoriService.find(HenteplanKategoriFindDto(henteplanId = id))
                        .fold(
                            { henteplan.right() },
                            { henteplan.copy(kategorier = it).right() }
                        )
                    }
                )
        }
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
        return transaction {
            val today = LocalDateTime.now()
            val avlystHenting: MutableList<PlanlagtHenting> = mutableListOf()
            findOne(dto.id)
                .fold(
                    { Either.left(ServiceError(it.message))},
                    { henteplan ->
                        planlagtHentingService.find(PlanlagtHentingFindDto(henteplanId = dto.id, after = today)).map {
                            it.map {
                                planlagtHentingService.delete(PlanlagtHentingDeleteDto(id = it.id))
                                if (it.avlyst != null) avlystHenting.add(it)
                            }
                        }
                            .fold({it.left()},
                                {
                                    henteplanKategoriService.find(HenteplanKategoriFindDto(henteplanId = dto.id)).map {
                                        it.map { henteplanKategoriService.delete(HenteplanKategoriDeleteDto(id = it.id)) }
                                    }.fold({it.left()},
                                            {

                                        var starttime = LocalDateTime.of(
                                            today.toLocalDate(),
                                            (dto.startTidspunkt ?: henteplan.startTidspunkt).toLocalTime()
                                        )

                                        if (starttime.isBefore(today)) {
                                            starttime = starttime.plusDays(1)
                                        }

                                        if (dto.startTidspunkt != null && dto.startTidspunkt.isAfter(today)) {
                                            starttime = dto.startTidspunkt
                                        } else if (dto.startTidspunkt == null && henteplan.startTidspunkt.isAfter(today)) {
                                            starttime = henteplan.startTidspunkt
                                        }

                                        appendPlanlagtHentinger(
                                            HenteplanSaveDto(
                                                avtaleId = henteplan.avtaleId,
                                                stasjonId = henteplan.stasjonId,
                                                startTidspunkt = starttime,
                                                sluttTidspunkt = dto.sluttTidspunkt ?: henteplan.sluttTidspunkt,
                                                ukedag = dto.ukedag ?: henteplan.ukedag,
                                                merknad = dto.merknad ?: henteplan.merknad,
                                                frekvens = dto.frekvens ?: henteplan.frekvens
                                            ), henteplan.id, henteplan
                                        ).fold(
                                            { Either.left(ServiceError(it.message)) },
                                            { it.planlagteHentinger?.map { planlagtHenting ->
                                                avlystHenting.map { avlystHenting ->
                                                    if (avlystHenting.startTidspunkt.toLocalDate().isEqual(planlagtHenting.startTidspunkt.toLocalDate()) && avlystHenting.sluttTidspunkt.toLocalDate().isEqual(planlagtHenting.sluttTidspunkt.toLocalDate()) ) {
                                                        planlagtHentingService.updateAvlystDate(id = planlagtHenting.id, date = avlystHenting.avlyst!!, aarsakId = avlystHenting.aarsakId!!, avlystAv = avlystHenting.avlystAv!!)
                                                    }
                                                }
                                            }
                                                appendKategorier(
                                                    HenteplanSaveDto(
                                                        avtaleId = henteplan.avtaleId,
                                                        stasjonId = henteplan.stasjonId,
                                                        startTidspunkt = starttime,
                                                        sluttTidspunkt = dto.sluttTidspunkt ?: henteplan.sluttTidspunkt,
                                                        ukedag = dto.ukedag ?: henteplan.ukedag,
                                                        merknad = dto.merknad ?: henteplan.merknad,
                                                        frekvens = dto.frekvens ?: henteplan.frekvens,
                                                        kategorier = dto.kategorier ?: henteplan.kategorier?.map {
                                                            HenteplanKategoriBatchSaveDto(
                                                                kategoriId = it.kategoriId,
                                                            )
                                                        }
                                                    ), henteplan.id, henteplan
                                                ).fold(
                                                    { Either.left(ServiceError(it.message)) },
                                                    { henteplanRepository.update(dto) }
                                                )
                                            }
                                        )
                                    })
                                })
                    }
                )
                .fold({rollback(); it.left()}, {it.right()})
        }
    }

    override fun archiveOne(id: UUID): Either<ServiceError, Unit> {
        return transaction {
            henteplanRepository.archiveOne(id)
                .map { henteplan ->
                    planlagtHentingService.archive(
                        PlanlagtHentingFindDto(
                            henteplanId = henteplan.id,
                            after = LocalDateTime.now()
                        )
                    )
                    .map { henteplanKategoriService.archive(
                        HenteplanKategoriFindDto(
                            henteplanId = henteplan.id
                        )
                    ) }.flatMap { it }

                }.flatMap { it }
                .fold({rollback(); it.left()}, {it.right()})
        }
    }

    override fun archive(params: HenteplanFindParams): Either<ServiceError, Unit> {
        return transaction {
            henteplanRepository.archive(params)
                .fold(
                    {Either.Left(ServiceError(it.message))},
                    { henteplaner ->
                        henteplaner.map { henteplan ->
                            planlagtHentingService.archive(
                                PlanlagtHentingFindDto(
                                    henteplanId = henteplan.id,
                                    after = LocalDateTime.now()
                                )
                            )
                                .map { henteplanKategoriService.archive(
                                    HenteplanKategoriFindDto(
                                        henteplanId = henteplan.id
                                    )
                                ) }.flatMap { it }
                        }
                            .sequence(Either.applicative())
                            .flatMap { Either.Right(Unit) }
                    }
                )
                .fold({rollback(); it.left()}, {it.right()})
        }
    }

}