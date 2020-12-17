package no.oslokommune.ombruk.uttaksforesporsel.model

import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.Serializable
import no.oslokommune.ombruk.partner.model.Partner
import no.oslokommune.ombruk.uttak.model.Uttak

@Serializable
data class UttaksForesporsel(
    @field:Schema(implementation = Uttak::class) val uttak: Uttak,
    @field:Schema(implementation = Partner::class) val partner: Partner
)

@Serializable
enum class UttaksForesporselStatus {
    AVVENTER,
    GODKJENT,
    AVVIST
}
