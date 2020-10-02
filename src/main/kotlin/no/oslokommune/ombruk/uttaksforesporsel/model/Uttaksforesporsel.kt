package no.oslokommune.ombruk.uttaksforesporsel.model

import kotlinx.serialization.Serializable
import no.oslokommune.ombruk.partner.model.Partner

@Serializable
data class Uttaksforesporsel(
    val pickup: Pickup,
    val partner: Partner
)
