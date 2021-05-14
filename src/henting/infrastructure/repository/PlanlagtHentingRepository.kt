package ombruk.backend.henting.infrastructure.repository

import ombruk.backend.core.infrastructure.RepositoryBase
import ombruk.backend.henting.domain.entity.PlanlagtHenting
import ombruk.backend.henting.domain.params.PlanlagtHentingCreateParams
import ombruk.backend.henting.domain.params.PlanlagtHentingFindParams
import ombruk.backend.henting.domain.params.PlanlagtHentingUpdateParams
import ombruk.backend.henting.domain.port.IPlanlagtHentingRepository
import ombruk.backend.henting.infrastructure.table.PlanlagtHentingTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.*
import java.util.*

class PlanlagtHentingRepository: RepositoryBase<PlanlagtHenting, PlanlagtHentingCreateParams, PlanlagtHentingUpdateParams, PlanlagtHentingFindParams>(),
    IPlanlagtHentingRepository{
    override fun insertQuery(params: PlanlagtHentingCreateParams): EntityID<UUID> {
        return PlanlagtHentingTable.insertAndGetId {
            it[startTidspunkt] = params.startTidspunkt
            it[sluttTidspunkt] = params.sluttTidspunkt
            it[merknad] = params.merknad
            it[henteplanId] = params.henteplanId
            it[avlyst] = null
        }
    }

    override fun updateQuery(params: PlanlagtHentingUpdateParams): Int {
        return table.update({table.id eq params.id}) { row ->
            params.startTidspunkt?.let { row[startTidspunkt] = it }
            params.sluttTidspunkt?.let { row[sluttTidspunkt] = it }
            params.merknad?.let { row[merknad] = it }
            params.avlyst?.let { row[avlyst] = it }
        }
    }

    override fun prepareQuery(params: PlanlagtHentingFindParams): Query {
        val query = table.selectAll()
        params.after?.let { query.andWhere { table.startTidspunkt.greaterEq(it) } }
        params.before?.let { query.andWhere { table.sluttTidspunkt.lessEq(it) } }
        params.henteplanId?.let { query.andWhere { table.henteplanId eq it } }
        params.avlyst?.let { query.andWhere { if(it) table.avlyst.isNotNull() else table.avlyst.isNull()} }
        params.merknad?.let { query.andWhere { table.merknad.like("%${it}%")} }
        return query
    }

    override fun toEntity(row: ResultRow): PlanlagtHenting {
        return PlanlagtHenting(
            row[table.id].value,
            row[table.startTidspunkt],
            row[table.sluttTidspunkt],
            row[table.merknad],
            row[table.henteplanId],
            row[table.avlyst]
        )
    }

    override val table = PlanlagtHentingTable
}