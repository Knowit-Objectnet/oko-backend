package ombruk.backend.model

import kotlinx.serialization.Serializable

@Serializable
data class Request(
    val pickupID: Int,
    val partner: Partner)
