package ombruk.backend.aktor.application.service

import arrow.core.*
import arrow.core.extensions.either.applicative.applicative
import arrow.core.extensions.list.traverse.sequence
import io.ktor.locations.*
import io.ktor.util.*
import notificationtexts.email.EmailDeletedKontaktMessage
import notificationtexts.sms.SMSDeletedKontaktMessage
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
                    verifiseringService.save(VerifiseringSaveDto(kontakt.id))
                        .flatMap { verifiseringService.getVerifiseringStatusById(it.id) }
                        .map { kontakt.copy(verifiseringStatus = it) }
                }.fold({rollback(); it.left()}, {it.right()})
        }
    }

    override fun getKontaktById(id: UUID): Either<ServiceError, Kontakt> {
        return transaction {
            kontaktRepository.findOne(id)
                .flatMap { kontakt ->
                    verifiseringService.getVerifiseringStatusById(kontakt.id)
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
                        verifiseringService.getVerifiseringStatusById(kontakt.id)
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
                notify(kontakt = kontakt)
                kontaktRepository.delete(id)
                    .bimap({ rollback(); it }, { kontakt })
            }
        }
    }

    @KtorExperimentalAPI
    override fun update(dto: KontaktUpdateDto): Either<ServiceError, Kontakt>  {
        return transaction {
            kontaktRepository.findOne(dto.id)
                .flatMap { original ->
                    kontaktRepository.update(dto)
                        .flatMap { updated ->
                            verifiseringService.update(
                                VerifiseringUpdateDto(
                                    id = updated.id,
                                    resetTelefon = (original.telefon != updated.telefon),
                                    resetEpost = (original.epost != updated.epost)
                                ))
                                .flatMap { verifiseringService.getVerifiseringStatusById(it.id) }
                                .map { updated.copy(verifiseringStatus = it) }
                        }
                }.fold({rollback(); it.left()}, {it.right()})
        }
    }

    private fun notify(kontakt: Kontakt): Kontakt {
        notificationService.sendMessage(
            SMSDeletedKontaktMessage.getInputParams(),
            EmailDeletedKontaktMessage.getInputParams(),
            listOf(kontakt)
        )
        return kontakt
    }
}