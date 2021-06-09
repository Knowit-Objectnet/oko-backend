package ombruk.backend.henting.infrastructure.repository

import ombruk.backend.aktor.infrastructure.table.StasjonTable
import ombruk.backend.core.infrastructure.RepositoryBase
import ombruk.backend.henting.domain.entity.Henteplan
import ombruk.backend.henting.domain.model.HenteplanFrekvens
import ombruk.backend.henting.domain.params.HenteplanCreateParams
import ombruk.backend.henting.domain.params.HenteplanFindParams
import ombruk.backend.henting.domain.params.HenteplanUpdateParams
import ombruk.backend.henting.domain.port.IHenteplanRepository
import ombruk.backend.henting.infrastructure.table.HenteplanTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import java.time.DayOfWeek
import java.util.*

class HenteplanRepository :
    RepositoryBase<Henteplan, HenteplanCreateParams, HenteplanUpdateParams, HenteplanFindParams>(),
    IHenteplanRepository {
    override fun insertQuery(params: HenteplanCreateParams): EntityID<UUID> {
        return HenteplanTable.insertAndGetId {
            it[avtaleId] = params.avtaleId
            it[stasjonId] = params.stasjonId
            it[frekvens] = params.frekvens.name
            it[startTidspunkt] = params.startTidspunkt
            it[sluttTidspunkt] = params.sluttTidspunkt
            it[ukedag] = params.ukedag?.value
            it[merknad] = params.merknad
        }
    }

    override fun updateQuery(params: HenteplanUpdateParams): Int {
        return table.update ( {table.id eq params.id} ) { row ->
            params.frekvens?.let { row[frekvens] = it.name }
            params.merknad?.let { row[merknad] = it }
            params.sluttTidspunkt?.let { row[sluttTidspunkt] = it }
            params.startTidspunkt?.let { row[startTidspunkt] = it }
            params.ukeDag?.let { row[ukedag] = it.value }
        }
    }

    override fun prepareQuery(params: HenteplanFindParams): Pair<Query, List<Alias<Table>>?> {
        val query = (table innerJoin StasjonTable).selectAll()
        params.avtaleId?.let { query.andWhere { table.avtaleId eq it } }
        params.frekvens?.let { query.andWhere { table.frekvens eq it.name } }
        params.stasjonId?.let { query.andWhere { table.stasjonId eq it } }
        params.ukedag?.let { query.andWhere { table.ukedag eq it.value } }
        params.after?.let { query.andWhere { table.startTidspunkt.greaterEq(it) } }
        params.before?.let { query.andWhere { table.sluttTidspunkt.lessEq(it) } }
        return Pair(query, null)
    }

    override fun toEntity(row: ResultRow, aliases: List<Alias<Table>>?): Henteplan {
        return Henteplan(
            row[table.id].value,
            row[table.avtaleId],
            row[table.stasjonId], // Add a mapper from Stasjon Table entity to actual entity
            HenteplanFrekvens.valueOf(row[table.frekvens]),
            row[table.startTidspunkt],
            row[table.sluttTidspunkt],
            row[table.ukedag]?.let { DayOfWeek.of(it) },
            row[table.merknad],
            emptyList()
        )
    }

    override fun archiveCondition(params: HenteplanFindParams): Op<Boolean>? {

        var myCond: Op<Boolean> = Op.TRUE
            .andIfNotNull(params.id){table.id eq params.id}
            .andIfNotNull(params.avtaleId){table.avtaleId eq params.avtaleId!!}
            .andIfNotNull(params.frekvens){table.frekvens eq params.frekvens!!.name}
            .andIfNotNull(params.stasjonId){table.stasjonId eq params.stasjonId!!}
            .andIfNotNull(params.ukedag){table.ukedag eq params.ukedag!!.value}
            .andIfNotNull(params.after){table.startTidspunkt.greaterEq(params.after!!)}
            .andIfNotNull(params.before){table.sluttTidspunkt.lessEq(params.before!!)}

        return myCond

        /*
        return when {
            params.id != null ->
                { table.id eq params.id }
            params.avtaleId != null ->
                { table.avtaleId eq params.avtaleId!!}
            else -> Op.TRUE //Should this archive all or none?
        }
        */

    }

    override val table = HenteplanTable
}