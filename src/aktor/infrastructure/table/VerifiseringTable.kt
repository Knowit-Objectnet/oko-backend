package ombruk.backend.aktor.infrastructure.table

import ombruk.backend.shared.database.ArchivableUUIDTable

object VerifiseringTable: ArchivableUUIDTable("verifisering") {
    val telefonKode = varchar("telefon_kode", 6).nullable()
    val telefonVerifisert = bool("telefon_verifisert")
    val epostKode = varchar("epost_kode", 6).nullable()
    val epostVerifisert = bool("epost_verifisert")
}