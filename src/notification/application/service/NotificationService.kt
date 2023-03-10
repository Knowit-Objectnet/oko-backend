package ombruk.backend.notification.application.service

import arrow.core.*
import arrow.core.extensions.either.foldable.fold
import ombruk.backend.aktor.application.api.dto.KontaktUpdateDto
import ombruk.backend.aktor.application.api.dto.VerifiseringSaveDto
import ombruk.backend.aktor.application.api.dto.VerifiseringUpdateDto
import ombruk.backend.aktor.application.service.IVerifiseringService
import ombruk.backend.aktor.domain.entity.Kontakt
import ombruk.backend.notification.domain.entity.Notification
import ombruk.backend.notification.domain.entity.SES
import ombruk.backend.notification.domain.entity.SNS
import ombruk.backend.notification.domain.entity.VerificationMessage
import ombruk.backend.notification.domain.params.SESInputParams
import ombruk.backend.notification.domain.params.SNSInputParams
import ombruk.backend.shared.error.ServiceError
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

class NotificationService constructor(
    private val snsService: SNSService,
    private val sesService: SESService,
    private val verifiseringService: IVerifiseringService
) : INotificationService {
    val logger: Logger = LoggerFactory.getLogger("ombruk.backend.notification.application.service.NotificationService")

    override fun sendMessage(snsInputParams: SNSInputParams, sesInputParams: SESInputParams, contacts: List<Kontakt>): Either<ServiceError, Notification> {
        val (numbers, addresses) = receivers(contacts)
        return invoke(snsInputParams, sesInputParams, numbers, addresses)
    }

    private fun receivers(receivers: List<Kontakt>): Pair<List<String>, List<String>> {
        val numbers: MutableList<String> = ArrayList()
        val addresses: MutableList<String> = ArrayList()

        receivers.map {
            val verified = verifiseringService.getVerifiseringById(it.id)
            if (verified.isRight()) {
                if (verified.fold({false}, {it.telefonVerifisert}) && !it.telefon.isNullOrBlank()) numbers.add(it.telefon)
                if (verified.fold({false}, {it.epostVerifisert}) && !it.epost.isNullOrBlank()) addresses.add(it.epost)

            }
        }

        // @TODO Validate input

        return Pair(numbers.toList(), addresses.toList())
    }

    private fun invoke(snsInputParams: SNSInputParams, sesInputParams: SESInputParams, numbers: List<String>, addresses: List<String>): Either<ServiceError, Notification> = runCatching {
        val sms = snsService.sendMessage(snsInputParams, numbers)
        val email = sesService.sendMessage(sesInputParams, addresses)
        if (sms.statusCode   != 200) throw Error("Invalid status for sms lambda invocation")
        if (email.statusCode != 200) throw Error("Invalid status for email lambda invocation")
    }
    .onFailure { logger.error("Lambda failed; ${it.message}") }
    .fold(
        { Notification(message = "Success").right() },
        { ServiceError(message = "Lambda invocation failed").left() }
    )

    override fun sendVerificationUpdated(dto: KontaktUpdateDto) = runCatching {

        val sms = dto.telefon?.let { verifySMS(dto.id, it) }?.map { it.message }
        val email = dto.epost?.let { verifyEmail(dto.id, it) }?.map { it.message }

        verifiseringService.update(
            VerifiseringUpdateDto(
                id = dto.id,
                telefonKode = sms?.let { it.getOrElse { null } },
                telefonVerifisert = sms?.let { false },
                epostKode = email?.let { it.getOrElse { null } },
                epostVerifisert = email?.let { false }
            )
        )
    }
    .onFailure { logger.error("Lambda failed for sendVerificationUpdated; ${it.message}") }
    .fold(
        { VerificationMessage("Success").right() },
        { ServiceError(message = "Lambda invocation failed").left() }
    )

    override fun sendVerification(contact: Kontakt): Either<ServiceError, VerificationMessage> = runCatching {
        val sms = contact.telefon?.let { verifySMS(contact.id, it) }?.map { it.message }
        val email = contact.epost?.let { verifyEmail(contact.id, it) }?.map { it.message }

        //TODO: Propagate errors from SNS and SES services? Right now it will still work even if one fails.

        verifiseringService.save(
            VerifiseringSaveDto(
                id = contact.id,
                telefonKode = sms?.let { it.getOrElse { null } },
                epostKode = email?.let { it.getOrElse { null } }
        ))

    }
    .onFailure { logger.error("Lambda failed for sendVerification; ${it.message}") }
    .fold(
        { VerificationMessage("Success").right() },
        { ServiceError(message = "Lambda invocation failed").left() }
    )

    override fun resendVerification(contact: Kontakt): Either<ServiceError, VerificationMessage> = runCatching {

        val sms =
            if (contact.telefon != null && !contact.verifiseringStatus!!.telefonVerifisert)
                verifySMS(contact.id, contact.telefon).map { it.message }
            else null

        val email =
            if (contact.epost != null && !contact.verifiseringStatus!!.epostVerifisert)
                verifyEmail(contact.id, contact.epost).map { it.message }
            else null

        if (sms?.isLeft() == true)
            throw Error(sms.fold({it.message}, {"Sending to SMS failed of unknown reason"}))
        else if (email?.isLeft() == true)
            throw Error(email.fold({it.message}, {"Sending to Email failed of unknown reason"}))
        else {
            verifiseringService.update(
                VerifiseringUpdateDto(
                    id = contact.id,
                    telefonKode = sms?.let { it.getOrElse { null } },
                    epostKode = email?.let { it.getOrElse { null } },
                )
            )
        }
    }
        .onFailure { logger.error("Lambda failed for sendVerification; ${it.message}") }
        .fold(
            { VerificationMessage("Success").right() },
            { ServiceError(message = "Lambda invocation failed").left() }
        )

    private fun verifySMS(id: UUID, number: String): Either<ServiceError, SNS> = runCatching {
        val sms = snsService.sendVerification(number)
        if (sms.statusCode != 200) throw Error("Invalid status for sms lambda invocation")
        sms
    }
    .onFailure { logger.error("Lambda failed; ${it.message}") }
    .fold(
        {it.right()},
        {ServiceError(it.message.orEmpty()).left()}
    )

    private fun verifyEmail(id: UUID, address: String): Either<ServiceError, SES> = runCatching {
        val email = sesService.sendVerification(address)
        if (email.statusCode != 200) throw Error("Invalid status for email lambda invocation")
        email
    }
    .onFailure { logger.error("Lambda failed; ${it.message}") }
    .fold(
        {it.right()},
        {ServiceError(it.message.orEmpty()).left()}
    )
}