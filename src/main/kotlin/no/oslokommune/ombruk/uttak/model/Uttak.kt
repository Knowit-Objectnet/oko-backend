package no.oslokommune.ombruk.uttak.model

import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import no.oslokommune.ombruk.stasjon.model.Stasjon
import no.oslokommune.ombruk.partner.model.Partner
import no.oslokommune.ombruk.shared.model.serializer.LocalDateTimeSerializer
import no.oslokommune.ombruk.uttaksdata.model.Uttaksdata
import java.time.LocalDateTime

@Serializable
@Schema(description = "Uttak that do not contain a partner is created by a Stasjon, and is meant to represent OMBRUKSDAGER")
data class Uttak(
    val id: Int,
    @field:Schema(
        description = "The date of the first (or only) Uttak"
    ) @Serializable(with = LocalDateTimeSerializer::class) val startTidspunkt: LocalDateTime,
    @field:Schema(
        description = "The date of the final Uttak. Must be after startTidspunkt. If the Uttak only occurs once," +
                "the date of sluttTidspunkt must correspond with startTidspunkt"
    ) @Serializable(with = LocalDateTimeSerializer::class) val sluttTidspunkt: LocalDateTime,
    @field:Schema(implementation = Stasjon::class) val stasjon: Stasjon,
    @field:Schema(
        nullable = true
    ) val partner: Partner?, // Optional partner. An uttak without a partner is arranged by the stasjon only.
    @field:Schema(
        implementation = GjentakelsesRegel::class,
        nullable = true
    ) var gjentakelsesRegel: GjentakelsesRegel? = null,
    @field:Schema(
        implementation = Uttaksdata::class,
        nullable = true
    ) var uttaksData: Uttaksdata? = null,
    @field:Schema(
        defaultValue = "GJENTAKENDE",
//        example = "ENKELT",
        implementation = UttaksType::class
    ) val type: UttaksType = UttaksType.GJENTAKENDE,
    @field:Schema(
        description = "A description of the Uttak",
        example = "Et uttak der det går skikkelig fort i svingene"
    ) val beskrivelse: String? = null,
    @field:Schema(
        description = "The date at which an Uttak was last changed",
        nullable = true
    ) @Serializable(with = LocalDateTimeSerializer::class) val endretTidspunkt: LocalDateTime? = null
)

@Serializable
@Schema(
    type = "string",
    description = "Describes the type of Uttak. ENKELT denotes a single Uttak. GJENTAKENDE is an Uttak that will occur" +
            "periodically, and are generated from a set of rules defined in a GjentakelsesRegel. EKSTRA is an Uttak" +
            "that was not originally planned for, and was added as a reaction to a cancellation or a surplus of inventory." +
            "OMBRUKSDAG is Uttak that are created by a Stasjon or a REG Administrator.",
    allowableValues = ["ENKELT", "GJENTAKENDE", "EKSTRA", "OMBRUKSDAG"]
)
enum class UttaksType {
    ENKELT,
    GJENTAKENDE,
    EKSTRA,
    OMBRUKSDAG
}
