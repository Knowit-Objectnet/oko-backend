package ombruk.backend.notification.application.service

import arrow.core.Either
import ombruk.backend.aktor.domain.entity.Kontakt
import ombruk.backend.notification.domain.entity.Notification
import ombruk.backend.notification.domain.entity.Verification
import ombruk.backend.shared.error.ServiceError

interface INotificationService {
    fun sendMessage(message: String, contacts: List<Kontakt>): Either<ServiceError, Notification>
    fun sendVerification(contact:Kontakt): Either<ServiceError, Verification>
}