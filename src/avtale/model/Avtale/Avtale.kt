package ombruk.backend.avtale.model.Avtale

import kotlinx.serialization.Serializable

enum class AvtaleType {
    FAST,
    ANNEN,
    OMBRUKS_ARRANGEMENT,
    INTERN_TRANSPORT
}

@Serializable()
data class Avtale (
    val id: Int,
    val aktorId: Int,
    val type: AvtaleType
)