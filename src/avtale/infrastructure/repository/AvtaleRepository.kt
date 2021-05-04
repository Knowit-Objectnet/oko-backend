package ombruk.backend.avtale.infrastructure.repository

import ombruk.backend.avtale.domain.entity.Avtale
import ombruk.backend.avtale.domain.params.AvtaleCreateParams
import ombruk.backend.avtale.domain.params.AvtaleFindParams
import ombruk.backend.avtale.domain.params.AvtaleUpdateParams
import ombruk.backend.avtale.domain.port.IAvtaleRepository
import ombruk.backend.avtale.infrastructure.table.AvtaleTable
import ombruk.backend.core.infrastructure.RepositoryBase
import ombruk.backend.henting.infrastructure.HenteplanTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*

class AvtaleRepository : RepositoryBase<Avtale, AvtaleCreateParams, AvtaleUpdateParams, AvtaleFindParams>(), IAvtaleRepository {
    override fun insertQuery(params: AvtaleCreateParams): EntityID<Int> {
        return table.insertAndGetId {
            it[aktorId] = params.aktorId
            it[type] = params.type
            it[startDato] = params.startDato
            it[sluttDato] = params.sluttDato
        }
    }

    override fun updateQuery(params: AvtaleUpdateParams): Int {
        TODO("Not yet implemented")
    }

    override fun prepareQuery(params: AvtaleFindParams): Query {
        val query = (HenteplanTable leftJoin table).selectAll()
        params.id?.let { query.andWhere { table.id eq it } }
        params.aktorId?.let { query.andWhere { table.aktorId eq it } }
        params.type?.let { query.andWhere { table.type eq it } }
        params.startDato?.let {query.andWhere { table.startDato eq it }}
        params.sluttDato?.let {query.andWhere { table.sluttDato eq it }}
        return query.groupBy(HenteplanTable.id)
    }

    override fun toEntity(row: ResultRow): Avtale {
        return Avtale(
            row[table.id].value,
            row[table.aktorId], // TODO: Figure out how to handle both Partner and Stasjon here.
            row[table.type],
            emptyList(),
            row[table.startDato],
            row[table.sluttDato]
        )
    }

    override val table = AvtaleTable
}