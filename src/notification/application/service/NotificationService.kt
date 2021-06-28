package ombruk.backend.notification.application.service

import arrow.core.*
import ombruk.backend.aktor.domain.entity.Kontakt
import ombruk.backend.notification.domain.entity.Notification
import ombruk.backend.shared.error.ServiceError
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class NotificationService constructor(
    private val snsService: SNSService,
    private val sesService: SESService
) : INotificationService {
    val logger: Logger = LoggerFactory.getLogger("ombruk.backend.notification.application.service.NotificationService")

    override fun sendMessage(message: String, contacts: List<Kontakt>): Either<ServiceError, Notification> {
        val (numbers, addresses) = receivers(contacts)
        return invoke(message, numbers, addresses)
    }

    private fun receivers(receivers: List<Kontakt>): Pair<List<String>, List<String>> {
        val numbers: MutableList<String> = ArrayList()
        val addresses: MutableList<String> = ArrayList()

        receivers.map {
            if (!it.telefon.isNullOrBlank()) numbers.add(it.telefon)
            if (!it.epost.isNullOrBlank()) addresses.add(it.epost)
        }

        // @TODO Validate input

        return Pair(numbers.toList(), addresses.toList())
    }

    private fun invoke(message: String, numbers: List<String>, addresses: List<String>): Either<ServiceError, Notification> = runCatching {
        val sms = snsService.sendMessage(message, numbers)
        val email = sesService.sendMessage(message, addresses)
        if (sms.statusCode   != 200) throw Error("Invalid status for lambda invocation")
        if (email.statusCode != 200) throw Error("Invalid status for lambda invocation")
    }
    .onFailure { logger.error("Lambda failed; ${it.message}") }
    .fold(
        { Notification(message = "Success").right() },
        { ServiceError(message = "Lambda invocation failed").left() }
    )
}