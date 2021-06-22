package ombruk.backend.henting.application.service

import arrow.core.*
import arrow.core.extensions.either.applicative.applicative
import arrow.core.extensions.list.traverse.sequence
import henting.application.api.dto.HenteplanSaveDto
import io.ktor.locations.*
import ombruk.backend.henting.application.api.dto.EkstraHentingDeleteDto
import ombruk.backend.henting.application.api.dto.EkstraHentingFindDto
import ombruk.backend.henting.application.api.dto.EkstraHentingSaveDto
import ombruk.backend.henting.application.api.dto.EkstraHentingUpdateDto
import ombruk.backend.henting.domain.entity.EkstraHenting
import ombruk.backend.henting.domain.entity.Henteplan
import ombruk.backend.henting.domain.params.EkstraHentingFindParams
import ombruk.backend.henting.domain.port.IEkstraHentingRepository
import ombruk.backend.kategori.application.api.dto.*
import ombruk.backend.kategori.application.service.EkstraHentingKategoriService
import ombruk.backend.kategori.application.service.IEkstraHentingKategoriService
import ombruk.backend.shared.error.ServiceError
import ombruk.backend.utlysning.application.api.dto.UtlysningFindDto
import ombruk.backend.utlysning.application.service.IUtlysningService
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

@KtorExperimentalLocationsAPI
class EkstraHentingService(
    val ekstraHentingRepository: IEkstraHentingRepository,
    val utlysningService: IUtlysningService,
    val ekstraHentingKategoriService: IEkstraHentingKategoriService
    ): IEkstraHentingService {

    fun appendKategorier(dto: EkstraHentingSaveDto, id: UUID, ekstraHenting: EkstraHenting): Either<ServiceError, EkstraHenting>
            = run {
        if (dto.kategorier == null) {return Either.Right(ekstraHenting)}
        val kategorier = dto.kategorier!!.map {
            ekstraHentingKategoriService.save(
                EkstraHentingKategoriSaveDto(
                    ekstraHentingId = id,
                    kategoriId = it.kategoriId,
                    mengde = it.mengde
                )
            )
        }
            .sequence(Either.applicative())
            .fix()
            .map { it.fix() }
            .fold({it.left()}, {it.right()})


        when (kategorier) {
            is Either.Left -> kategorier
            is Either.Right -> ekstraHenting.copy(kategorier = kategorier.b).right()
        }
    }

    override fun save(dto: EkstraHentingSaveDto): Either<ServiceError, EkstraHenting> {
        return transaction {
            ekstraHentingRepository.insert(dto)
                .fold(
                    {Either.Left(ServiceError(it.message))},
                    {
                        appendKategorier(dto, it.id, it)
                    }
                )
                .fold({ rollback(); it.left() }, { it.right() })
        }
    }

    override fun findOne(id: UUID): Either<ServiceError, EkstraHenting> {
        return transaction {
            ekstraHentingRepository.findOne(id)
                .flatMap { ekstraHenting ->
                    utlysningService.findAccepted(ekstraHentingId = ekstraHenting.id)
                        .map { utlysning -> ekstraHenting.copy(godkjentUtlysning = utlysning) }
                }
                .flatMap { ekstraHenting ->
                    ekstraHentingKategoriService.find(EkstraHentingKategoriFindDto(ekstraHentingId = id))
                        .fold({ ekstraHenting.right() },
                            { ekstraHenting.copy(kategorier = it).right() }
                        )
                }
        }
    }

    override fun find(dto: EkstraHentingFindDto): Either<ServiceError, List<EkstraHenting>> {
        return transaction {
            ekstraHentingRepository.find(dto)
                .flatMap { list ->
                    list.map { ekstraHenting ->
                        utlysningService.findAccepted(ekstraHentingId = ekstraHenting.id)
                            .map { utlysning -> ekstraHenting.copy(godkjentUtlysning = utlysning) }
                    }
                        .sequence(Either.applicative())
                        .fix()
                        .map { it.fix() }
                }
                .flatMap { list ->
                    list.map { ekstraHenting ->
                        ekstraHentingKategoriService.find(EkstraHentingKategoriFindDto(ekstraHentingId = ekstraHenting.id))
                            .fold(
                                { ekstraHenting.right() },
                                {ekstraHenting.copy(kategorier = it).right()}
                            )
                    }.sequence(Either.applicative()).fix().map { it.fix() }
                }
        }
    }

    override fun delete(dto: EkstraHentingDeleteDto): Either<ServiceError, Unit> {
        return transaction { ekstraHentingRepository.delete(dto.id) }
    }

    override fun update(dto: EkstraHentingUpdateDto): Either<ServiceError, EkstraHenting> {
        return transaction {
            findOne(dto.id)
                .fold(
                    {Either.left(ServiceError(it.message))},
                    {ekstraHenting ->
                        ekstraHentingKategoriService.find(EkstraHentingKategoriFindDto(ekstraHentingId = dto.id)).map {
                            it.map { ekstraHentingKategoriService.delete(EkstraHentingKategoriDeleteDto(id = it.id)) }
                        }
                            .fold({it.left()},
                                {
                                    appendKategorier(
                                        EkstraHentingSaveDto(
                                            startTidspunkt = dto.startTidspunkt ?: ekstraHenting.startTidspunkt,
                                            sluttTidspunkt = dto.sluttTidspunkt ?: ekstraHenting.sluttTidspunkt,
                                            merknad = dto.merknad ?: ekstraHenting.merknad,
                                            stasjonId = ekstraHenting.stasjonId,
                                            kategorier = dto.kategorier ?: ekstraHenting.kategorier?.map {
                                                EkstraHentingKategoriBatchSaveDto(
                                                    kategoriId = it.kategoriId,
                                                    mengde = it.mengde
                                                )
                                            }
                                        ), ekstraHenting.id, ekstraHenting
                                    ).fold(
                                        { Either.left(ServiceError(it.message)) },
                                        { ekstraHentingRepository.update(dto) }
                                    )
                                }
                            )
                    }
                )
                .fold({rollback(); it.left()}, {it.right()})
        }
    }

    override fun archive(params: EkstraHentingFindParams): Either<ServiceError, Unit> {
        return transaction {
            ekstraHentingRepository.archive(params)
                .fold(
                    {Either.Left(ServiceError(it.message))},
                    { hentinger ->
                        hentinger.map { utlysningService.archive(UtlysningFindDto(hentingId = it.id)) }
                            .sequence(Either.applicative())
                            .fix()
                            .map { it.fix() }
                            .flatMap { Either.Right(Unit) }
                    }
                )
                .fold({rollback(); it.left()}, {it.right()})
        }
    }

    override fun archiveOne(id: UUID): Either<ServiceError, Unit> {
        return transaction {
            ekstraHentingRepository.archiveOne(id)
                .fold(
                    {Either.Left(ServiceError(it.message))},
                    {utlysningService.archive(UtlysningFindDto(hentingId = it.id))}
                )
                .fold({rollback(); it.left()}, {it.right()})
        }
    }
}