package ombruk.backend.aktor.domain.entity

import java.util.*

interface Aktor {
    val id: UUID
    var navn: String
    var kontaktPersoner: List<Kontakt>
}