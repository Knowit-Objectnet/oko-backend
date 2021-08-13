package ombruk.backend.henting.application.service

import arrow.core.*
import arrow.core.extensions.either.applicative.applicative
import arrow.core.extensions.list.traverse.sequence
import io.ktor.locations.*
import notificationtexts.email.EmailAvlystMessage
import notificationtexts.email.SMSAvlystMessage
import ombruk.backend.aarsak.application.service.IAarsakService
import ombruk.backend.aktor.application.api.dto.KontaktGetDto
import ombruk.backend.aktor.application.service.IKontaktService
import ombruk.backend.aktor.domain.entity.Kontakt
import ombruk.backend.henting.application.api.dto.*
import ombruk.backend.henting.domain.entity.PlanlagtHenting
import ombruk.backend.henting.domain.params.PlanlagtHentingFindParams
import ombruk.backend.henting.domain.port.IPlanlagtHentingRepository
import ombruk.backend.notification.application.service.INotificationService
import ombruk.backend.shared.error.ServiceError
import ombruk.backend.vektregistrering.application.api.dto.VektregistreringFindDto
import ombruk.backend.vektregistrering.application.service.IVektregistreringService
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.LocalDateTime
import java.util.*

@KtorExperimentalLocationsAPI
class PlanlagtHentingService(val planlagtHentingRepository: IPlanlagtHentingRepository, val vektregistreringService: IVektregistreringService, val notificationService: INotificationService, val aarsakService: IAarsakService, val kontaktService: IKontaktService): IPlanlagtHentingService, KoinComponent {
    private val henteplanService: IHenteplanService by inject()

    override fun save(dto: PlanlagtHentingSaveDto): Either<ServiceError, PlanlagtHenting> {
        return transaction { planlagtHentingRepository.insert(dto) }
    }

    override fun findOne(id: UUID): Either<ServiceError, PlanlagtHenting> {
        return transaction {
            planlagtHentingRepository.findOne(id)
                .fold(
                    { Either.Left(ServiceError(it.message)) },
                    {
                        it.let { planlagtHenting ->
                            henteplanService.findOne(planlagtHenting.henteplanId)
                                .fold(
                                    {
                                        vektregistreringService.find(VektregistreringFindDto(hentingId = planlagtHenting.id)).fold(
                                            { planlagtHenting.right() },
                                            { planlagtHenting.copy(vektregistreringer = it).right() }
                                        )
                                    },
                                    { henteplan ->
                                        vektregistreringService.find(VektregistreringFindDto(hentingId = planlagtHenting.id)).fold(
                                            { planlagtHenting.copy(kategorier = henteplan.kategorier).right() },
                                            { planlagtHenting.copy(kategorier = henteplan.kategorier, vektregistreringer = it).right() })
                                    }
                                )
                        }
                    }
                )
        }
    }

    override fun find(dto: PlanlagtHentingFindDto): Either<ServiceError, List<PlanlagtHenting>> {
        return transaction {
            planlagtHentingRepository.find(dto)
                .fold(
                    { Either.Left(ServiceError(it.message)) },
                    {
                        it.map { planlagtHenting ->
                            henteplanService.findOne(planlagtHenting.henteplanId)
                                .fold(
                                    {
                                        vektregistreringService.find(VektregistreringFindDto(hentingId = planlagtHenting.id)).fold(
                                            { planlagtHenting.right() },
                                            { planlagtHenting.copy(vektregistreringer = it).right() }
                                        )
                                    },
                                    { henteplan ->
                                        vektregistreringService.find(VektregistreringFindDto(hentingId = planlagtHenting.id)).fold(
                                            { planlagtHenting.copy(kategorier = henteplan.kategorier).right() },
                                            { planlagtHenting.copy(kategorier = henteplan.kategorier, vektregistreringer = it).right() })
                                    }
                                )
                        }.sequence(Either.applicative()).fix().map { it.fix() }
                    }
                )
        }
    }

    override fun delete(dto: PlanlagtHentingDeleteDto): Either<ServiceError, Unit> {
        return transaction { planlagtHentingRepository.delete(dto.id) }
    }

    override fun update(dto: PlanlagtHentingUpdateDto): Either<ServiceError, PlanlagtHenting> {
        return transaction { planlagtHentingRepository.update(dto) }
    }

    override fun update(dto: PlanlagtHentingUpdateDto, avlystAv: UUID): Either<ServiceError, PlanlagtHenting> {
        return transaction {
            planlagtHentingRepository.update(dto, avlystAv).flatMap { planlagtHenting ->
                if (planlagtHenting.aarsakId != null) notify(planlagtHenting).right()
                else planlagtHenting.right()
            }
        }
    }

    override fun batchSaveForHenteplan(dto: PlanlagtHentingBatchPostDto): Either<ServiceError, List<PlanlagtHenting>> {
        return transaction {
            dto.dateList.map {
                planlagtHentingRepository.insert(
                    PlanlagtHentingSaveDto(
                        henteplanId = dto.saveDto.henteplanId,
                        startTidspunkt = LocalDateTime.of(it, dto.saveDto.startTidspunkt.toLocalTime()),
                        sluttTidspunkt = LocalDateTime.of(it, dto.saveDto.sluttTidspunkt.toLocalTime()),
                    ))
            }
                .sequence(Either.applicative())
                .fix()
                .map { it.fix() }
                .fold({ rollback(); it.left() }, { it.right() })
        }
    }

    override fun archiveOne(id: UUID): Either<ServiceError, Unit> {
        return transaction {
            planlagtHentingRepository.archiveOne(id)
                .fold({ rollback(); it.left() }, { Either.right(Unit) })
        }
    }

    override fun archive(params: PlanlagtHentingFindParams): Either<ServiceError, Unit> {
        return transaction {
            planlagtHentingRepository.archive(params)
                .fold({ rollback(); it.left() }, { Either.right(Unit) })
        }
    }

    override fun updateAvlystDate(id: UUID, date: LocalDateTime, aarsakId: UUID, avlystAv: UUID): Either<ServiceError, PlanlagtHenting> {
        return transaction {
            planlagtHentingRepository.updateAvlystDate(id, date, aarsakId, avlystAv)
        }
    }

    private fun notify(henting: PlanlagtHenting): PlanlagtHenting {

        val stasjonKontaker = mutableListOf<Kontakt>()
        val partnerKontaker = mutableListOf<Kontakt>()

        kontaktService.getKontakter(KontaktGetDto(aktorId = henting.stasjonId)).map { stasjonKontaker.addAll(it) }
        kontaktService.getKontakter(KontaktGetDto(aktorId = henting.aktorId)).map { partnerKontaker.addAll(it) }

        aarsakService.findOne(henting.aarsakId!!).flatMap {
            notificationService.sendMessage(
                    SMSAvlystMessage.getInputParams(henting, it),
                    EmailAvlystMessage.getInputParams(henting, it),
                    (stasjonKontaker.toList() + partnerKontaker.toList())
            )
        }

        return henting
    }

}