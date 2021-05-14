package ombruk.backend.aktor.infrastructure.repository

import ombruk.backend.aktor.domain.entity.Partner
import ombruk.backend.aktor.domain.enum.PartnerStorrelse
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
            it[navn] = params.navn
            it[storrelse] = params.storrelse.name
            it[ideell] = params.ideell
        }
    }

    override fun updateQuery(params: PartnerUpdateParams): Int {
        return table.update({ PartnerTable.id eq params.id })
        { row ->
            params.navn?.let { row[navn] = it }
            params.storrelse?.let { row[storrelse] = it.name }
            params.ideell?.let { row[ideell] = it }
        }
    }

    override fun prepareQuery(params: PartnerFindParams): Query {
        val query = (table).selectAll()
        params.navn?.let { query.andWhere { table.navn eq it } }
        params.ideell?.let { query.andWhere { table.ideell eq it } }
        params.storrelse?.let { query.andWhere { table.storrelse eq it.name } }
        return query
    }

    override fun toEntity(row: ResultRow): Partner {
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
            PartnerStorrelse.valueOf(row[table.storrelse]),
            row[table.ideell]
        )
    }

    override val table: PartnerTable = PartnerTable

}