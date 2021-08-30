package ombruk.backend.aktor.domain.entity

import kotlinx.serialization.Serializable
import ombruk.backend.avtale.domain.entity.Avtale
import shared.model.serializer.UUIDSerializer
import java.util.*

@Serializable
data class Partner(
    @Serializable(with = UUIDSerializer::class) override var id: UUID,
    override var navn: String,
    override var kontaktPersoner: List<Kontakt>,
    var avtaler: List<Avtale> = emptyList(),
    var ideell: Boolean
): IAktor