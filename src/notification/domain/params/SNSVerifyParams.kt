package ombruk.backend.notification.domain.params

import kotlinx.serialization.Serializable

@Serializable
data class SNSVerifyParams(
    val number: String
)