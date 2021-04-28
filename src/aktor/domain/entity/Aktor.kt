package ombruk.backend.aktor.domain.entity

interface Aktor {
    val id: Int
    var navn: String
    var kontaktPersoner: List<Kontakt>
}