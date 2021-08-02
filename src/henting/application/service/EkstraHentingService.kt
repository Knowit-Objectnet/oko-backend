package ombruk.backend.henting.application.service

import arrow.core.*
import arrow.core.extensions.either.applicative.applicative
import arrow.core.extensions.list.traverse.sequence
import io.ktor.locations.*
import ombruk.backend.henting.application.api.dto.EkstraHentingDeleteDto
import ombruk.backend.henting.application.api.dto.EkstraHentingFindDto
import ombruk.backend.henting.application.api.dto.EkstraHentingSaveDto
import ombruk.backend.henting.application.api.dto.EkstraHentingUpdateDto
import ombruk.backend.henting.domain.entity.EkstraHenting
import ombruk.backend.henting.domain.params.EkstraHentingFindParams
import ombruk.backend.henting.domain.port.IEkstraHentingRepository
import ombruk.backend.kategori.application.api.dto.EkstraHentingKategoriBatchSaveDto
import ombruk.backend.kategori.application.api.dto.EkstraHentingKategoriDeleteDto
import ombruk.backend.kategori.application.api.dto.EkstraHentingKategoriFindDto
import ombruk.backend.kategori.application.api.dto.EkstraHentingKategoriSaveDto
import ombruk.backend.kategori.application.service.IEkstraHentingKategoriService
import ombruk.backend.shared.error.ServiceError
import ombruk.backend.utlysning.application.api.dto.UtlysningBatchSaveDto
import ombruk.backend.utlysning.application.api.dto.UtlysningFindDto
import ombruk.backend.utlysning.application.service.IUtlysningService
import ombruk.backend.vektregistrering.application.api.dto.VektregistreringFindDto
import ombruk.backend.vektregistrering.application.service.IVektregistreringService
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

@KtorExperimentalLocationsAPI
class EkstraHentingService(
    val ekstraHentingRepository: IEkstraHentingRepository,
    val utlysningService: IUtlysningService,
    val ekstraHentingKategoriService: IEkstraHentingKategoriService,
    val vektregistreringService: IVektregistreringService
): IEkstraHentingService {

    fun appendKategorier(dto: EkstraHentingSaveDto, id: UUID, ekstraHenting: EkstraHenting): Either<ServiceError, EkstraHenting>
            = run {
        if (dto.kategorier == null) {return Either.Right(ekstraHenting)}

        dto.kategorier!!.map {
            ekstraHentingKategoriService.save(
                EkstraHentingKategoriSaveDto(
                ekstraHentingId = id,
                    kategoriId = it.kategoriId,
                    mengde = it.mengde
            ))
        }
            .sequence(Either.applicative())
            .fix()
            .map { it.fix() }
            .fold({it.left()},{ekstraHenting.copy(kategorier = it).right()})
    }

    override fun save(dto: EkstraHentingSaveDto): Either<ServiceError, EkstraHenting> {
        return transaction {
            ekstraHentingRepository.insert(dto)
                .flatMap{ appendKategorier(dto, it.id, it) }
                .flatMap { henting ->
                    if (dto.partnere != null) {
                        utlysningService.batchSave(
                            UtlysningBatchSaveDto(
                                hentingId = henting.id,
                                partnerIds = dto.partnere!!
                            )
                        ).flatMap {
                            henting.copy(utlysninger = it).right()
                        }
                    }
                    else {
                        henting.right()
                    }
                }
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
                        .fold({
                            vektregistreringService.find(VektregistreringFindDto(hentingId = ekstraHenting.id)).fold(
                            {ekstraHenting.right()},
                            {ekstraHenting.copy(vektregistreringer = it).right()}
                        )},
                            { kategorier ->
                                vektregistreringService.find(VektregistreringFindDto(hentingId = ekstraHenting.id)).fold(
                                    {ekstraHenting.copy(kategorier = kategorier).right()},
                                    {ekstraHenting.copy(vektregistreringer = it, kategorier = kategorier).right()}
                                )
                            }
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
                .flatMap { if (dto.aktorId == null) it.right()
                            else it.filter { it.godkjentUtlysning != null && it.godkjentUtlysning.partnerId == dto.aktorId }.right()
                }
                .flatMap { list ->
                    list.map { ekstraHenting ->
                        ekstraHentingKategoriService.find(EkstraHentingKategoriFindDto(ekstraHentingId = ekstraHenting.id))
                            .fold(
                                { vektregistreringService.find(VektregistreringFindDto(hentingId = ekstraHenting.id)).fold(
                                    {ekstraHenting.right()},
                                    {ekstraHenting.copy(vektregistreringer = it).right()}
                                )},
                                {kategorier ->
                                    vektregistreringService.find(VektregistreringFindDto(hentingId = ekstraHenting.id)).fold(
                                        {ekstraHenting.copy(kategorier = kategorier).right()},
                                        {ekstraHenting.copy(vektregistreringer = it, kategorier = kategorier).right()}
                                    )}
                            )
                    }.sequence(Either.applicative()).fix().map { it.fix() }
                }
        }
    }

    override fun findWithUtlysninger(dto: EkstraHentingFindDto, aktorId: UUID?): Either<ServiceError, List<EkstraHenting>> {
        return transaction {
            find(dto)
                .flatMap { list ->
                    list.map { ekstraHenting ->
                        utlysningService.find(UtlysningFindDto(hentingId = ekstraHenting.id, partnerId = aktorId))
                            .flatMap { utlysninger -> ekstraHenting.copy(utlysninger = utlysninger).right() }

                    }.sequence(Either.applicative()).fix().map { it.fix() }.map { if (aktorId == null) it else it.filter { it.utlysninger.size == 1 } }
                }
        }
    }

    override fun findOneWithUtlysninger(id: UUID, aktorId: UUID?): Either<ServiceError, EkstraHenting> {
           return transaction {
               findOne(id)
                   .flatMap {ekstrahenting ->
                        utlysningService.find(UtlysningFindDto(hentingId = id))
                            .flatMap { utlysninger ->  ekstrahenting.copy(utlysninger = (aktorId?.let { utlysninger.filter { it.partnerId == aktorId } } ?: utlysninger )).right()}
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
                                            beskrivelse = dto.beskrivelse ?: ekstraHenting.beskrivelse,
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