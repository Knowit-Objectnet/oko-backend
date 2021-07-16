package ombruk.backend.aktor.application.service

import arrow.core.*
import arrow.core.extensions.either.applicative.applicative
import arrow.core.extensions.list.traverse.sequence
import io.ktor.locations.*
import io.ktor.util.*
import ombruk.backend.aktor.application.api.dto.*
import ombruk.backend.aktor.domain.entity.Kontakt
import ombruk.backend.aktor.domain.entity.VerifiseringStatus
import ombruk.backend.aktor.domain.port.IKontaktRepository
import ombruk.backend.notification.application.service.INotificationService
import ombruk.backend.notification.domain.entity.VerificationMessage
import ombruk.backend.shared.error.ServiceError
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class KontaktService constructor(
    private val kontaktRepository: IKontaktRepository,
    private val notificationService: INotificationService,
    private val verifiseringService: IVerifiseringService
) :
    IKontaktService {

    @KtorExperimentalAPI
    override fun save(dto: KontaktSaveDto): Either<ServiceError, Kontakt> {
        return transaction {
            kontaktRepository.insert(dto)
                .flatMap { kontakt ->
                    notificationService.sendVerification(kontakt)
                        .flatMap {
                            verifiseringService.getVerifisertById(kontakt.id)
                                .fold({kontakt.right()}, {kontakt.copy(verifiseringStatus = it).right()})
                        }
                }
        }
    }

    override fun getKontaktById(id: UUID): Either<ServiceError, Kontakt> {
        return transaction {
            kontaktRepository.findOne(id)
                .flatMap { kontakt ->
                    verifiseringService.getVerifisertById(kontakt.id)
                        .fold({kontakt.right()}, {kontakt.copy(verifiseringStatus = it).right()})
                }
        }
    }

    @KtorExperimentalLocationsAPI
    override fun getKontakter(dto: KontaktGetDto): Either<ServiceError, List<Kontakt>> {
        return transaction {
            kontaktRepository.find(dto)
                .flatMap { kontakter ->
                    kontakter.map { kontakt ->
                        verifiseringService.getVerifisertById(kontakt.id)
                            .fold({ kontakt.right() }, { kontakt.copy(verifiseringStatus = it).right() })
                    }.sequence(Either.applicative()).fix().map { it.fix() }
                }
        }
    }

    @KtorExperimentalLocationsAPI
    override fun verifiserKontakt(dto: KontaktVerifiseringDto): Either<ServiceError, VerifiseringStatus> {
        return verifiseringService.verifiser(dto)
    }

    override fun resendVerifikasjon(kontakt: Kontakt): Either<ServiceError, VerificationMessage> {
        return notificationService.resendVerification(kontakt)
    }

    @KtorExperimentalAPI
    override fun deleteKontaktById(id: UUID): Either<ServiceError, Kontakt> {
        return transaction {
            getKontaktById(id).flatMap { kontakt ->
                kontaktRepository.delete(id)
                    .bimap({ rollback(); it }, { kontakt })
            }
        }
    }

    @KtorExperimentalAPI
    override fun update(dto: KontaktUpdateDto): Either<ServiceError, Kontakt>  {
        return transaction {
            kontaktRepository.findOne(dto.id)
                .flatMap<ServiceError, Kontakt, Kontakt> { original ->
                    kontaktRepository.update(dto)
                        .flatMap { updated ->
                            notificationService.sendVerificationUpdated(
                                dto.copy(
                                    telefon = if (original.telefon != updated.telefon) dto.telefon else null,
                                    epost = if (original.epost != updated.epost) dto.epost else null
                                )
                            )
                                .flatMap {
                                    verifiseringService.getVerifisertById(updated.id)
                                        .flatMap { updated.copy(verifiseringStatus = it).right() }
                                }
                        }
                }.fold({rollback(); it.left()}, {it.right()})
        }
    }
}