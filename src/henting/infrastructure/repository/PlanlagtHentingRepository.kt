package ombruk.backend.henting.infrastructure.repository

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ombruk.backend.aktor.infrastructure.table.StasjonTable
import ombruk.backend.avtale.infrastructure.table.AvtaleTable
import ombruk.backend.core.infrastructure.RepositoryBase
import ombruk.backend.henting.domain.entity.PlanlagtHenting
import ombruk.backend.henting.domain.entity.PlanlagtHentingWithParents
import ombruk.backend.henting.domain.params.PlanlagtHentingCreateParams
import ombruk.backend.henting.domain.params.PlanlagtHentingFindParams
import ombruk.backend.henting.domain.params.PlanlagtHentingUpdateParams
import ombruk.backend.henting.domain.port.IPlanlagtHentingRepository
import ombruk.backend.henting.infrastructure.table.HenteplanTable
import ombruk.backend.henting.infrastructure.table.PlanlagtHentingTable
import ombruk.backend.shared.error.RepositoryError
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
        params.id?.let { query.andWhere { table.id eq it } }
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

    override fun findWithParents(params: PlanlagtHentingFindParams):
            Either<RepositoryError, List<PlanlagtHentingWithParents>> {
        return runCatching {
            val joinedTable = table.innerJoin(HenteplanTable, {table.henteplanId}, {HenteplanTable.id})
                .innerJoin(AvtaleTable, {HenteplanTable.avtaleId}, {AvtaleTable.id})
                .innerJoin(StasjonTable, {HenteplanTable.stasjonId}, {StasjonTable.id})
            val query = joinedTable.selectAll()
            params.id?.let { query.andWhere { table.id eq it } }
            params.after?.let { query.andWhere { table.startTidspunkt.greaterEq(it) } }
            params.before?.let { query.andWhere { table.sluttTidspunkt.lessEq(it) } }
            params.henteplanId?.let { query.andWhere { table.henteplanId eq it } }
            params.avlyst?.let { query.andWhere { if(it) table.avlyst.isNotNull() else table.avlyst.isNull()} }
            params.merknad?.let { query.andWhere { table.merknad.like("%${it}%")} }
            query.mapNotNull { toPlanlagtHentingWithParents(it) }
        }.fold(
            {it.right()},
            { RepositoryError.SelectError(it.message).left() }
        )
    }

    fun toPlanlagtHentingWithParents(row: ResultRow): PlanlagtHentingWithParents {
        return PlanlagtHentingWithParents(
            row[table.id].value,
            row[table.startTidspunkt],
            row[table.sluttTidspunkt],
            row[table.merknad],
            row[table.henteplanId],
            row[table.avlyst],
            row[AvtaleTable.id].value,
            row[StasjonTable.id].value,
            row[StasjonTable.navn]
        )
    }

    override val table = PlanlagtHentingTable
}