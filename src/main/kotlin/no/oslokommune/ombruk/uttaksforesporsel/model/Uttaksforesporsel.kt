package no.oslokommune.ombruk.uttaksforesporsel.model

import kotlinx.serialization.Serializable
import no.oslokommune.ombruk.partner.model.Partner
import no.oslokommune.ombruk.uttak.model.Uttak

@Serializable
data class Uttaksforesporsel(
        val uttak: Uttak,
        val partner: Partner
)
