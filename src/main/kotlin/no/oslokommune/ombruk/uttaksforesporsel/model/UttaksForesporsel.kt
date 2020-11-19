package no.oslokommune.ombruk.uttaksforesporsel.model

import kotlinx.serialization.Serializable
import no.oslokommune.ombruk.partner.model.Partner
import no.oslokommune.ombruk.uttak.model.Uttak

@Serializable
data class UttaksForesporsel(
        val uttak: Uttak,
        val partner: Partner
)
@Serializable
enum class UttaksForesporselStatus {
        AVVENTER,
        GODKJENT,
        AVVIST
}
