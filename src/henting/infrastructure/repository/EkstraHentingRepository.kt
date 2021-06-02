package ombruk.backend.henting.infrastructure.repository

import ombruk.backend.core.infrastructure.RepositoryBase
import ombruk.backend.henting.domain.entity.EkstraHenting
import ombruk.backend.henting.domain.params.EkstraHentingCreateParams
import ombruk.backend.henting.domain.params.EkstraHentingFindParams
import ombruk.backend.henting.domain.params.EkstraHentingUpdateParams
import ombruk.backend.henting.domain.port.IEkstraHentingRepository
import ombruk.backend.henting.infrastructure.table.EkstraHentingTable
import ombruk.backend.henting.infrastructure.table.HenteplanTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import java.util.*

class EkstraHentingRepository :
    RepositoryBase<EkstraHenting, EkstraHentingCreateParams, EkstraHentingUpdateParams, EkstraHentingFindParams>(),
    IEkstraHentingRepository {
    override fun insertQuery(params: EkstraHentingCreateParams): EntityID<UUID> {
        return HenteplanTable.insertAndGetId {
            it[table.startTidspunkt] = params.startTidspunkt
            it[table.sluttTidspunkt] = params.sluttTidspunkt
            it[table.merknad] = params.merknad
            it[table.stasjonId] = params.stasjonId
        }
    }

    override fun updateQuery(params: EkstraHentingUpdateParams): Int {
        return table.update ( {table.id eq params.id} ) { row ->
            params.startTidspunkt?.let { row[table.startTidspunkt] = it }
            params.sluttTidspunkt?.let { row[table.sluttTidspunkt] = it }
            params.merknad?.let { row[table.merknad] = it }
        }
    }

    override fun prepareQuery(params: EkstraHentingFindParams): Pair<Query, List<Alias<Table>>?> {
        val query = table.selectAll()
        params.id?.let { query.andWhere { table.id eq it } }
        params.stasjonId?.let { query.andWhere { table.stasjonId eq it } }
        params.after?.let { query.andWhere { table.startTidspunkt.greaterEq(it) } }
        params.before?.let { query.andWhere { table.sluttTidspunkt.lessEq(it) } }
        return Pair(query, null)
    }

    override fun toEntity(row: ResultRow, aliases: List<Alias<Table>>?): EkstraHenting {
        return EkstraHenting(
            row[table.id].value,
            row[table.startTidspunkt],
            row[table.sluttTidspunkt],
            row[table.merknad],
            row[table.stasjonId]
        )
    }

    override val table = EkstraHentingTable
}