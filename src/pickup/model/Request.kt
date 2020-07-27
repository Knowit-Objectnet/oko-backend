package ombruk.backend.pickup.model

import kotlinx.serialization.Serializable
import ombruk.backend.partner.model.Partner

@Serializable
data class Request(
    val pickupID: Int,
    val partner: Partner
)
