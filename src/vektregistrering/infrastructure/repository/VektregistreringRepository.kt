package ombruk.backend.vektregistrering.infrastructure.repository

import arrow.core.Either
import arrow.core.left
import ombruk.backend.core.infrastructure.RepositoryBase
import ombruk.backend.henting.domain.entity.PlanlagtHenting
import ombruk.backend.henting.domain.params.PlanlagtHentingUpdateParams
import ombruk.backend.henting.infrastructure.table.PlanlagtHentingTable
import ombruk.backend.kategori.domain.entity.Kategori
import ombruk.backend.kategori.domain.params.KategoriCreateParams
import ombruk.backend.kategori.domain.params.KategoriFindParams
import ombruk.backend.kategori.domain.port.IKategoriRepository
import ombruk.backend.kategori.infrastructure.table.KategoriTable
import ombruk.backend.shared.error.RepositoryError
import ombruk.backend.vektregistrering.domain.entity.Vektregistrering
import ombruk.backend.vektregistrering.domain.params.VektregistreringCreateParams
import ombruk.backend.vektregistrering.domain.params.VektregistreringFindParams
import ombruk.backend.vektregistrering.domain.params.VektregistreringUpdateParams
import ombruk.backend.vektregistrering.domain.port.IVektregistreringRepository
import ombruk.backend.vektregistrering.infrastructure.table.VektregistreringTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class VektregistreringRepository : RepositoryBase<Vektregistrering, VektregistreringCreateParams, VektregistreringUpdateParams, VektregistreringFindParams>(), IVektregistreringRepository {
    override fun insertQuery(params: VektregistreringCreateParams): EntityID<UUID> {
        return table.insertAndGetId {
            it[hentingId] = params.hentingId
            it[kategoriId] = params.kategoriId
            it[vekt] = params.vekt
            it[registreringsDato] = LocalDateTime.now()
            params.vektRegistreringAv?.let { vektAv -> it[vektRegistreringAv] = vektAv }
        }
    }

    override fun prepareQuery(params: VektregistreringFindParams): Pair<Query, List<Alias<Table>>?> {
        val query = table.innerJoin(KategoriTable, {table.kategoriId}, {id}).selectAll()
        params.id?.let { query.andWhere { table.id eq it } }
        params.hentingId?.let { query.andWhere { table.hentingId eq it } }
        params.kategoriId?.let { query.andWhere { table.kategoriId eq it } }
        params.vekt?.let { query.andWhere { table.vekt eq it } }
        params.after?.let { query.andWhere { table.registreringsDato.greaterEq(it) } }
        params.before?.let { query.andWhere { table.registreringsDato.lessEq(it) } }
        params.vektRegistreringAv?.let { query.andWhere { table.vektRegistreringAv eq it } }
        return Pair(query, null)
    }

    override fun findOneMethod(id: UUID): List<Vektregistrering> {
        val joinedTable = table.innerJoin(KategoriTable, {table.kategoriId}, { KategoriTable.id })
        return joinedTable.select { table.id eq id }.mapNotNull { toEntity(it) }
    }

    override fun toEntity(row: ResultRow, aliases: List<Alias<Table>>?): Vektregistrering {
        return Vektregistrering(
            row[table.id].value,
            row[table.hentingId],
            row[table.kategoriId],
            row[KategoriTable.navn],
            row[table.vekt],
            row[table.registreringsDato],
            row[table.vektRegistreringAv]
        )
    }

    override val table = VektregistreringTable

     override fun updateQuery(params: VektregistreringUpdateParams): Int {
        return table.update({table.id eq params.id}) { row ->
            params.vekt?.let {
                row[vekt] = it;
            }
            params.vektRegistreringAv?.let { row[vektRegistreringAv] = params.vektRegistreringAv!! }
            row[registreringsDato] = LocalDateTime.now()
        }
    }
}