package no.oslokommune.ombruk.pickup.model

import kotlinx.serialization.Serializable
import no.oslokommune.ombruk.partner.model.Partner

@Serializable
data class Request(
    val pickup: Pickup,
    val partner: Partner
)
