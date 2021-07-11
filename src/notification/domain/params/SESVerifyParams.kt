package ombruk.backend.notification.domain.params

import kotlinx.serialization.Serializable

@Serializable
data class SESVerifyParams(
    val address: String
)