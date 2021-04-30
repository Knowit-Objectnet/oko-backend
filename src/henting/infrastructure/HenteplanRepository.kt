package ombruk.backend.henting.infrastructure

import ombruk.backend.aktor.infrastructure.table.StasjonTable
import ombruk.backend.core.infrastructure.RepositoryBase
import ombruk.backend.henting.domain.entity.Henteplan
import ombruk.backend.henting.domain.params.HenteplanCreateParams
import ombruk.backend.henting.domain.params.HenteplanFindParams
import ombruk.backend.henting.domain.params.HenteplanUpdateParams
import ombruk.backend.henting.domain.port.IHenteplanRepository
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import java.time.DayOfWeek

class HenteplanRepository :
    RepositoryBase<Henteplan, HenteplanCreateParams, HenteplanUpdateParams, HenteplanFindParams>(),
    IHenteplanRepository {
    override fun insertQuery(params: HenteplanCreateParams): EntityID<Int> {
        return HenteplanTable.insertAndGetId {
            it[avtaleId] = params.avtaleId!!
            it[stasjonId] = params.stasjonId
            it[frekvens] = params.frekvens
            it[startTidspunkt] = params.startTidspunkt
            it[sluttTidspunkt] = params.sluttTidspunkt
            it[ukedag] = params.ukedag.value
            it[merknad] = params.merknad
        }
    }

    override fun updateQuery(params: HenteplanUpdateParams): Int {
        TODO("Not yet implemented")
    }

    override fun prepareQuery(params: HenteplanFindParams): Query {
        val query = (table innerJoin StasjonTable).selectAll()
        params.avtaleId?.let { query.andWhere { table.avtaleId eq it } }
        params.avtaleIds?.let { query.andWhere { table.avtaleId.inList(it) } }
        params.frekvens?.let { query.andWhere { table.frekvens eq it } }
        params.stasjonId?.let { query.andWhere { table.stasjonId eq it } }
        params.ukedag?.let { query.andWhere { table.ukedag eq it.value } }
        params.after?.let { query.andWhere { table.startTidspunkt.greaterEq(it) } }
        params.before?.let { query.andWhere { table.startTidspunkt.lessEq(it) } }
        return query
    }

    override fun toEntity(row: ResultRow): Henteplan {
        return Henteplan(
            row[table.id].value,
            row[table.avtaleId],
            row[table.stasjonId],
            row[table.frekvens],
            row[table.startTidspunkt],
            row[table.sluttTidspunkt],
            DayOfWeek.of(row[table.ukedag]),
            row[HenteplanTable.merknad]
        )
    }

    override val table = HenteplanTable
}