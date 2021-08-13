package ombruk.backend.aktor.domain.entity

import kotlinx.serialization.Serializable
import ombruk.backend.aktor.domain.enum.StasjonType
import shared.model.serializer.UUIDSerializer
import java.util.*

@Serializable
data class Stasjon (
    @Serializable (with = UUIDSerializer::class) override val id: UUID,
    override var navn: String,
    override var kontaktPersoner: List<Kontakt>,
    val type: StasjonType
): IAktor