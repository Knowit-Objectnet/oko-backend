package ombruk.backend.aktor.domain.entity

interface Aktor {
    var id: Int
    var navn: String
    var kontaktPersoner: List<Kontakt>
}