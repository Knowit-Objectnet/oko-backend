package ombruk.backend.notification

import ombruk.backend.notification.application.service.*
import org.koin.dsl.module

val notificationModule = module(createdAtStart = true) {
    single { SNSService() }
    single { SESService() }
    single<INotificationService> { NotificationService(get(), get()) }
}