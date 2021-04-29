package ombruk.backend.avtale.infrastructure.repository

import ombruk.backend.avtale.domain.entity.Avtale
import ombruk.backend.avtale.domain.entity.Henteplan
import ombruk.backend.avtale.domain.model.*
import ombruk.backend.avtale.domain.port.IAvtaleRepository
import ombruk.backend.avtale.domain.port.IHenteplanRepository
import ombruk.backend.avtale.infrastructure.table.AvtaleTable
import ombruk.backend.avtale.infrastructure.table.HenteplanTable
import ombruk.backend.core.infrastructure.RepositoryBase
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import java.time.LocalTime

class HenteplanRepository : RepositoryBase<Henteplan, HenteplanCreateParams, HenteplanUpdateParams, HenteplanFindParams>(),
    IHenteplanRepository {
    override fun insertQuery(params: HenteplanCreateParams): EntityID<Int> {
        return table.insertAndGetId {
            it[avtaleId] = params.avtaleId
            it[stasjonId] = params.stasjonId
            it[frekvens] = params.frekvens
            it[startTidspunkt] = params.startTidspunkt.toString()
            it[sluttTidspunkt] = params.sluttTidspunkt.toString()
            it[ukeDag] = params.ukeDag
            it[startDato] = params.startDato
            it[sluttDato] = params.sluttDato
            it[merknad] = params.merknad
        }
    }

    override fun updateQuery(params: HenteplanUpdateParams): Int {
        return table.update({ table.id eq params.id }) { row ->
            params.avtaleId?.let { row[avtaleId] = it }
            params.stasjonId?.let { row[stasjonId] = it }
            params.frekvens?.let { row[frekvens] = it }
            params.startTidspunkt?.let { row[startTidspunkt] = it.toString() }
            params.sluttTidspunkt?.let { row[sluttTidspunkt] = it.toString() }
            params.ukeDag?.let { row[ukeDag] = it }
            params.startDato?.let { row[startDato] = it }
            params.sluttDato?.let { row[sluttDato] = it }
            params.merknad?.let { row[merknad] = it }
        }
    }

    override fun prepareQuery(params: HenteplanFindParams): Query {
        val query = table.selectAll()

        params.avtaleId?.let { query.andWhere { table.avtaleId eq it } }
        params.stasjonId?.let { query.andWhere { table.stasjonId eq it } }
        params.frekvens?.let { query.andWhere { table.frekvens eq it } }
        params.startTidspunkt?.let { query.andWhere { table.startTidspunkt eq it.toString() } }
        params.sluttTidspunkt?.let { query.andWhere { table.sluttTidspunkt eq it.toString() } }
        params.ukeDag?.let { query.andWhere { table.ukeDag eq it } }
        params.startDato?.let { query.andWhere { table.startDato eq it } }
        params.sluttDato?.let { query.andWhere { table.sluttDato eq it } }
        params.merknad?.let { query.andWhere { table.merknad eq it } }

        return query
    }

    override fun toEntity(row: ResultRow): Henteplan {
        return Henteplan(
            row[HenteplanTable.id].value,
            row[HenteplanTable.avtaleId],
            row[HenteplanTable.stasjonId],
            row[HenteplanTable.frekvens],
            LocalTime.parse(row[HenteplanTable.startTidspunkt]),
            LocalTime.parse(row[HenteplanTable.sluttTidspunkt]),
            row[HenteplanTable.ukeDag],
            row[HenteplanTable.startDato],
            row[HenteplanTable.sluttDato],
            row[HenteplanTable.merknad]
            )
    }

    override val table = HenteplanTable

}