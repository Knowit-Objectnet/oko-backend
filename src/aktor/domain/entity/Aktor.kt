package ombruk.backend.aktor.domain.entity

import kotlinx.serialization.Serializable
import ombruk.backend.aktor.domain.enum.AktorType
import shared.model.serializer.UUIDSerializer
import java.util.*

@Serializable
data class Aktor(
    @Serializable(with = UUIDSerializer::class) override var id: UUID,
    override var navn: String,
    override var kontaktPersoner: List<Kontakt>,
    var aktorType: AktorType
): IAktor

interface IAktor {
    val id: UUID
    var navn: String
    var kontaktPersoner: List<Kontakt>
}