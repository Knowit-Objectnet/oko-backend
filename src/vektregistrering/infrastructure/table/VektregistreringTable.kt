package ombruk.backend.vektregistrering.infrastructure.table

import ombruk.backend.henting.infrastructure.table.EkstraHentingTable
import ombruk.backend.henting.infrastructure.table.HenteplanTable
import ombruk.backend.henting.infrastructure.table.PlanlagtHentingTable
import ombruk.backend.henting.infrastructure.table.PlanlagtHentingTable.nullable
import ombruk.backend.kategori.infrastructure.table.KategoriTable
import ombruk.backend.shared.database.ArchivableUUIDTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.`java-time`.datetime

object VektregistreringTable : ArchivableUUIDTable("vektregistrering"){
    val hentingId = uuid("henting_id").references(PlanlagtHentingTable.id).references(EkstraHentingTable.id)
    val kategoriId = uuid("kategori_id").references(KategoriTable.id)
    val vekt = float("vekt")
    val registreringsDato = datetime("registrerings_dato")
    val vektRegistreringAv = uuid("vekt_registrering_av")
}