package ombruk.backend.aktor.infrastructure.repository

import ombruk.backend.aktor.domain.entity.Partner
import ombruk.backend.aktor.domain.model.PartnerCreateParams
import ombruk.backend.aktor.domain.model.PartnerFindParams
import ombruk.backend.aktor.domain.model.PartnerUpdateParams
import ombruk.backend.aktor.domain.port.IPartnerRepository
import ombruk.backend.aktor.infrastructure.table.PartnerTable
import ombruk.backend.core.infrastructure.RepositoryBase
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import java.util.*

class PartnerRepository : RepositoryBase<Partner, PartnerCreateParams, PartnerUpdateParams, PartnerFindParams>(),
    IPartnerRepository {
    override fun insertQuery(params: PartnerCreateParams): EntityID<UUID> {
        return table.insertAndGetId {
            params.id.let { paramId -> it[id] = paramId }
            it[navn] = params.navn
            it[ideell] = params.ideell
        }
    }

    override fun updateQuery(params: PartnerUpdateParams): Int {
        return table.update({ PartnerTable.id eq params.id })
        { row ->
            params.navn?.let { row[navn] = it }
            params.ideell?.let { row[ideell] = it }
        }
    }

    override fun prepareQuery(params: PartnerFindParams): Pair<Query, List<Alias<Table>>?> {
        val query = (table).selectAll()
        params.navn?.let { query.andWhere { table.navn eq it } }
        params.ideell?.let { query.andWhere { table.ideell eq it } }
        return Pair(query, null)
    }

    override fun toEntity(row: ResultRow, aliases: List<Alias<Table>>?): Partner {
//        if (!row.hasValue(PartnerTable.id) || row.getOrNull(
//                PartnerTable.id
//            ) == null
//        ) {
//            return null
//        }

        return Partner(
            row[table.id].value,
            row[table.navn],
            emptyList(),
            row[table.ideell]
        )
    }

    override val table: PartnerTable = PartnerTable

}