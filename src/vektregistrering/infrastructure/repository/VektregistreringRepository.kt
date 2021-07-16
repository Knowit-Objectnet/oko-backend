package ombruk.backend.vektregistrering.infrastructure.repository

import ombruk.backend.core.infrastructure.RepositoryBase
import ombruk.backend.kategori.domain.entity.Kategori
import ombruk.backend.kategori.domain.params.KategoriCreateParams
import ombruk.backend.kategori.domain.params.KategoriFindParams
import ombruk.backend.kategori.domain.port.IKategoriRepository
import ombruk.backend.kategori.infrastructure.table.KategoriTable
import ombruk.backend.vektregistrering.domain.entity.Vektregistrering
import ombruk.backend.vektregistrering.domain.params.VektregistreringCreateParams
import ombruk.backend.vektregistrering.domain.params.VektregistreringFindParams
import ombruk.backend.vektregistrering.domain.port.IVektregistreringRepository
import ombruk.backend.vektregistrering.infrastructure.table.VektregistreringTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class VektregistreringRepository : RepositoryBase<Vektregistrering, VektregistreringCreateParams, Nothing, VektregistreringFindParams>(), IVektregistreringRepository {
    override fun insertQuery(params: VektregistreringCreateParams): EntityID<UUID> {
        return table.insertAndGetId {
            it[hentingId] = params.hentingId
            it[kategoriId] = params.kategoriId
            it[vekt] = params.vekt
            it[registreringsDato] = LocalDateTime.now()
        }
    }

    override fun prepareQuery(params: VektregistreringFindParams): Pair<Query, List<Alias<Table>>?> {
        val query = table.selectAll()
        params.id?.let { query.andWhere { table.id eq it } }
        params.hentingId?.let { query.andWhere { table.hentingId eq it } }
        params.kategoriId?.let { query.andWhere { table.kategoriId eq it } }
        params.vekt?.let { query.andWhere { table.vekt eq it } }
        params.after?.let { query.andWhere { table.registreringsDato.greaterEq(it) } }
        params.before?.let { query.andWhere { table.registreringsDato.lessEq(it) } }
        return Pair(query, null)
    }

    override fun toEntity(row: ResultRow, aliases: List<Alias<Table>>?): Vektregistrering {
        return Vektregistrering(
            row[table.id].value,
            row[table.hentingId],
            row[table.kategoriId],
            row[table.vekt],
            row[table.registreringsDato]
        )
    }

    override fun updateQuery(params: Nothing): Int {
        TODO("Not yet implemented")
    }

    override val table = VektregistreringTable
}