package ombruk.backend.avtale.domain.entity

import ombruk.backend.avtale.domain.enum.AvtaleType

data class Avtale(
    val id: Int,
    val aktorId: Int,
    val type: AvtaleType
    //TODO: Should Avtale have a list of Henteplan?
)
