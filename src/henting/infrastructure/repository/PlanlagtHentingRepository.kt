package ombruk.backend.henting.infrastructure.repository

import arrow.core.Either
import arrow.core.left
import ombruk.backend.aktor.infrastructure.table.PartnerTable
import ombruk.backend.aktor.infrastructure.table.StasjonTable
import ombruk.backend.avtale.infrastructure.table.AvtaleTable
import ombruk.backend.core.infrastructure.RepositoryBase
import ombruk.backend.henting.domain.entity.PlanlagtHentingWithParents
import ombruk.backend.henting.domain.params.PlanlagtHentingCreateParams
import ombruk.backend.henting.domain.params.PlanlagtHentingFindParams
import ombruk.backend.henting.domain.params.PlanlagtHentingUpdateParams
import ombruk.backend.henting.domain.port.IPlanlagtHentingRepository
import ombruk.backend.henting.infrastructure.table.HenteplanTable
import ombruk.backend.henting.infrastructure.table.PlanlagtHentingTable
import ombruk.backend.shared.error.RepositoryError
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.lang.Exception
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class PlanlagtHentingRepository: RepositoryBase<PlanlagtHentingWithParents, PlanlagtHentingCreateParams, PlanlagtHentingUpdateParams, PlanlagtHentingFindParams>(),
    IPlanlagtHentingRepository{
    override fun insertQuery(params: PlanlagtHentingCreateParams): EntityID<UUID> {
        return PlanlagtHentingTable.insertAndGetId {
            it[startTidspunkt] = params.startTidspunkt
            it[sluttTidspunkt] = params.sluttTidspunkt
            it[henteplanId] = params.henteplanId
            it[avlyst] = null
            it[aarsak] = null
        }
    }

    fun updateQuery(params: PlanlagtHentingUpdateParams, avlystId: UUID): Int {
        return table.update({table.id eq params.id}) { row ->
            params.startTidspunkt?.let { row[startTidspunkt] = it }
            params.sluttTidspunkt?.let { row[sluttTidspunkt] = it }
            params.avlys?.let {
                if (it) {row[avlyst] = LocalDateTime.now(); row[avlystAv] = avlystId}
                else {row[avlyst] = null; row[aarsak] = null; row[avlystAv] = null}
            }
            params.aarsak?.let { value ->
                if (params.avlys != null && !params.avlys!!)
                else row[aarsak] = value
            }
        }
    }

    override fun prepareQuery(params: PlanlagtHentingFindParams): Pair<Query, List<Alias<Table>>?> {
        val stasjonAlias = StasjonTable
            .alias("stasjonAktorAlias")
        val joinedTable = table.innerJoin(HenteplanTable, {table.henteplanId}, {HenteplanTable.id})
            .innerJoin(AvtaleTable, {HenteplanTable.avtaleId}, {AvtaleTable.id})
            .innerJoin(StasjonTable, {HenteplanTable.stasjonId}, {StasjonTable.id})
            .leftJoin(PartnerTable, {AvtaleTable.aktorId}, {PartnerTable.id})
            .leftJoin(stasjonAlias, {AvtaleTable.aktorId}, {stasjonAlias[StasjonTable.id]})
        val query = joinedTable.selectAll()
        params.id?.let { query.andWhere { table.id eq it } }
        params.after?.let { query.andWhere { table.startTidspunkt.greaterEq(it) } }
        params.before?.let { query.andWhere { table.sluttTidspunkt.lessEq(it) } }
        params.henteplanId?.let { query.andWhere { table.henteplanId eq it } }
        params.avlyst?.let { query.andWhere { if(it) table.avlyst.isNotNull() else table.avlyst.isNull()} }
        return Pair(query, listOf(stasjonAlias))
    }

    override fun findOneMethod(id: UUID): List<PlanlagtHentingWithParents> {
        val stasjonAlias = StasjonTable
            .alias("stasjonAktorAlias")
        val joinedTable = table.innerJoin(HenteplanTable, {table.henteplanId}, {HenteplanTable.id})
            .innerJoin(AvtaleTable, {HenteplanTable.avtaleId}, {AvtaleTable.id})
            .innerJoin(StasjonTable, {HenteplanTable.stasjonId}, {StasjonTable.id})
            .leftJoin(PartnerTable, {AvtaleTable.aktorId}, {PartnerTable.id})
            .leftJoin(stasjonAlias, {AvtaleTable.aktorId}, {stasjonAlias[StasjonTable.id]})
        return joinedTable.select { table.id eq id }.mapNotNull { toEntity(it, listOf(stasjonAlias)) }
    }

    override fun toEntity(row: ResultRow, aliases: List<Alias<Table>>?): PlanlagtHentingWithParents {

        val stasjonAlias = aliases?.get(0)!!

        val partnerAktorId = row.getOrNull(PartnerTable.id)
        val stasjonAktorId = row.getOrNull(stasjonAlias[StasjonTable.id])
        lateinit var aktorId: UUID
        lateinit var aktorNavn: String

        if (partnerAktorId != null) {
            aktorId = partnerAktorId.value
            aktorNavn = row[PartnerTable.navn]
        } else {

            if (stasjonAktorId == null) throw Exception("PlanlagtHenting has no aktor")

            aktorId = row[stasjonAlias[StasjonTable.id]].value
            aktorNavn = row[stasjonAlias[StasjonTable.navn]]
        }

        return PlanlagtHentingWithParents(
            row[table.id].value,
            row[table.startTidspunkt],
            row[table.sluttTidspunkt],
            //FIXME: GET FROM HENTEPLAN
            "",
            row[table.henteplanId],
            row[table.avlyst],
            row[table.avlystAv],
            row[table.aarsak],
            row[AvtaleTable.id].value,
            aktorId,
            aktorNavn,
            row[StasjonTable.id].value,
            row[StasjonTable.navn],
            emptyList()
        )
    }

    override val table = PlanlagtHentingTable

    override fun archiveCondition(params: PlanlagtHentingFindParams): Op<Boolean>? {
        return Op.TRUE
            .andIfNotNull(params.id){table.id eq params.id}
            .andIfNotNull(params.henteplanId){table.henteplanId eq params.henteplanId!!}
            .andIfNotNull(params.after){table.startTidspunkt.greaterEq(params.after!!)}
            .andIfNotNull(params.before){table.sluttTidspunkt.lessEq(params.before!!)}
            .andIfNotNull(params.avlyst){if(params.avlyst!!) {table.avlyst.isNotNull()} else {table.avlyst.isNull()} }
            //.andIfNotNull(params.merknad){Op.FALSE} //Not implemented: Adding this so any calls including just merknad will not archive everything.
    }

    override fun update(
        params: PlanlagtHentingUpdateParams,
        avlystId: UUID
    ): Either<RepositoryError, PlanlagtHentingWithParents> {
        return runCatching {
            updateQuery(params, avlystId)
        }
            .onFailure { logger.error("Failed to update database; ${it.message}") }
            .fold(
                {
                    findOne(params.id)
                },
                { RepositoryError.UpdateError(it.message).left() }
            )
    }

    override fun updateAvlystDate(id: UUID, date: LocalDateTime, aarsakMelding: String?, avlystAvId: UUID): Either<RepositoryError, PlanlagtHentingWithParents> {
        fun u(id: UUID, date: LocalDateTime, aarsakMelding: String?, avlystAvId: UUID): Int {
            return table.update( {table.id eq id} ) { row ->
                row[avlyst] = date
                aarsakMelding?.let { row[aarsak] = it }
                row[avlystAv] = avlystAvId
            }
        }
        return runCatching {
            u(id, date, aarsakMelding, avlystAvId)
        }
            .onFailure { logger.error("Failed to update database; ${it.message}") }
            .fold(
                {
                    findOne(id)
                },
                { RepositoryError.UpdateError(it.message).left() }
            )
    }

    override fun updateQuery(params: PlanlagtHentingUpdateParams): Int {
        return table.update({table.id eq params.id}) { row ->
            params.startTidspunkt?.let { row[startTidspunkt] = it }
            params.sluttTidspunkt?.let { row[sluttTidspunkt] = it }
            params.avlys?.let {
                if (it) {row[avlyst] = LocalDateTime.now();}
                else {row[avlyst] = null; row[aarsak] = null; row[avlystAv] = null}
            }
            params.aarsak?.let { value ->
                if (params.avlys != null && !params.avlys!!)
                else row[aarsak] = value
            }
        }
    }
}