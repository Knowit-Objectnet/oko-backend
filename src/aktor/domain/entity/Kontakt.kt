package ombruk.backend.aktor.domain.entity

data class Kontakt (
    val id: Int,
    val navn: String,
    val telefon: String,
    val rolle: String
)