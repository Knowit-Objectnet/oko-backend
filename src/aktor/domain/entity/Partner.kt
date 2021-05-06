package ombruk.backend.aktor.domain.entity

import kotlinx.serialization.Serializable
import ombruk.backend.aktor.domain.enum.PartnerStorrelse
import shared.model.serializer.UUIDSerializer
import java.util.*

@Serializable(with = UUIDSerializer::class)
data class Partner (
    override var id: UUID,
    override var navn: String,
    override var kontaktPersoner: List<Kontakt> = emptyList(),
    var storrelse: PartnerStorrelse,
    var ideell: Boolean
): Aktor