package ombruk.backend.henting.infrastructure.repository

import ombruk.backend.aktor.infrastructure.table.PartnerTable
import ombruk.backend.aktor.infrastructure.table.StasjonTable
import ombruk.backend.core.infrastructure.RepositoryBase
import ombruk.backend.henting.domain.entity.EkstraHenting
import ombruk.backend.henting.domain.params.EkstraHentingCreateParams
import ombruk.backend.henting.domain.params.EkstraHentingFindParams
import ombruk.backend.henting.domain.params.EkstraHentingUpdateParams
import ombruk.backend.henting.domain.port.IEkstraHentingRepository
import ombruk.backend.henting.infrastructure.table.EkstraHentingTable
import ombruk.backend.utlysning.infrastructure.table.UtlysningTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import java.util.*

class EkstraHentingRepository :
    RepositoryBase<EkstraHenting, EkstraHentingCreateParams, EkstraHentingUpdateParams, EkstraHentingFindParams>(),
    IEkstraHentingRepository {
    override fun insertQuery(params: EkstraHentingCreateParams): EntityID<UUID> {
        return EkstraHentingTable.insertAndGetId {
            it[table.startTidspunkt] = params.startTidspunkt
            it[table.sluttTidspunkt] = params.sluttTidspunkt
            it[table.beskrivelse] = params.beskrivelse
            it[table.stasjonId] = params.stasjonId
        }
    }

    override fun updateQuery(params: EkstraHentingUpdateParams): Int {
        return table.update ( {table.id eq params.id} ) { row ->
            params.startTidspunkt?.let { row[table.startTidspunkt] = it }
            params.sluttTidspunkt?.let { row[table.sluttTidspunkt] = it }
            params.beskrivelse?.let { row[table.beskrivelse] = it }
        }
    }

    override fun prepareQuery(params: EkstraHentingFindParams): Pair<Query, List<Alias<Table>>?> {
        val joinedTable = table.innerJoin(StasjonTable, {table.stasjonId}, {StasjonTable.id})
        val query = joinedTable.selectAll()
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
            row[table.beskrivelse],
            row[table.stasjonId],
            row[StasjonTable.navn],
            null,
            emptyList()
        )
    }

    override fun findOneMethod(id: UUID): List<EkstraHenting> {
        val joinedTable = table.innerJoin(StasjonTable, {table.stasjonId}, {StasjonTable.id})
        return joinedTable.select{table.id eq id}.mapNotNull { toEntity(it) }
    }

    override fun archiveCondition(params: EkstraHentingFindParams): Op<Boolean>? {
        return Op.TRUE
            .andIfNotNull(params.id){table.id eq params.id}
            .andIfNotNull(params.stasjonId){table.stasjonId eq params.stasjonId!!}
            .andIfNotNull(params.before){table.sluttTidspunkt.lessEq(params.before!!)}
            .andIfNotNull(params.after){table.startTidspunkt.greaterEq(params.after!!)}
            .andIfNotNull(params.beskrivelse){Op.FALSE} //Not implemented
    }

    override val table = EkstraHentingTable
}