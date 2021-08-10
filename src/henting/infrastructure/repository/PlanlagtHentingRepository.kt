package ombruk.backend.henting.infrastructure.repository

import arrow.core.Either
import arrow.core.left
import ombruk.backend.aktor.domain.entity.Stasjon
import ombruk.backend.aktor.infrastructure.table.PartnerTable
import ombruk.backend.aktor.infrastructure.table.StasjonTable
import ombruk.backend.avtale.infrastructure.table.AvtaleTable
import ombruk.backend.core.infrastructure.RepositoryBase
import ombruk.backend.henting.domain.entity.PlanlagtHenting
import ombruk.backend.henting.domain.params.PlanlagtHentingCreateParams
import ombruk.backend.henting.domain.params.PlanlagtHentingFindParams
import ombruk.backend.henting.domain.params.PlanlagtHentingUpdateParams
import ombruk.backend.henting.domain.port.IPlanlagtHentingRepository
import ombruk.backend.henting.infrastructure.table.HenteplanTable
import ombruk.backend.henting.infrastructure.table.PlanlagtHentingTable
import ombruk.backend.shared.error.RepositoryError
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import java.lang.Exception
import java.time.LocalDateTime
import java.util.*

class PlanlagtHentingRepository: RepositoryBase<PlanlagtHenting, PlanlagtHentingCreateParams, PlanlagtHentingUpdateParams, PlanlagtHentingFindParams>(),
    IPlanlagtHentingRepository{
    override fun insertQuery(params: PlanlagtHentingCreateParams): EntityID<UUID> {
        return PlanlagtHentingTable.insertAndGetId {
            it[startTidspunkt] = params.startTidspunkt
            it[sluttTidspunkt] = params.sluttTidspunkt
            it[henteplanId] = params.henteplanId
            it[avlyst] = null
            it[aarsakId] = null
        }
    }

    fun updateQuery(params: PlanlagtHentingUpdateParams, avlystId: UUID): Int {
        return table.update({table.id eq params.id}) { row ->
            params.startTidspunkt?.let { row[startTidspunkt] = it }
            params.sluttTidspunkt?.let { row[sluttTidspunkt] = it }
            params.avlyst?.let {
                if (it) {row[avlyst] = LocalDateTime.now(); row[avlystAv] = avlystId}
                else {row[avlyst] = null; row[aarsakId] = null; row[avlystAv] = null}
            }
            params.aarsakId?.let { value ->
                if (params.avlyst != null && !params.avlyst!!)
                else row[aarsakId] = value
            }
        }
    }

    override fun prepareQuery(params: PlanlagtHentingFindParams): Pair<Query, List<Alias<Table>>?> {
        val stasjonAlias = StasjonTable
            .alias("stasjonAktorAlias")
        val joinedTable = table
            .innerJoin(HenteplanTable, {table.henteplanId}, {HenteplanTable.id})
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
        params.aktorId?.let { query.andWhere { PartnerTable.id eq it }.orWhere { stasjonAlias[StasjonTable.id] eq it }}
        params.stasjonId?.let { query.andWhere { StasjonTable.id eq it }}
        return Pair(query, listOf(stasjonAlias))
    }

    override fun findOneMethod(id: UUID): List<PlanlagtHenting> {
        val stasjonAlias = StasjonTable
            .alias("stasjonAktorAlias")
        val joinedTable = table.innerJoin(HenteplanTable, {table.henteplanId}, {HenteplanTable.id})
            .innerJoin(AvtaleTable, {HenteplanTable.avtaleId}, {AvtaleTable.id})
            .innerJoin(StasjonTable, {HenteplanTable.stasjonId}, {StasjonTable.id})
            .leftJoin(PartnerTable, {AvtaleTable.aktorId}, {PartnerTable.id})
            .leftJoin(stasjonAlias, {AvtaleTable.aktorId}, {stasjonAlias[StasjonTable.id]})
        return joinedTable.select { table.id eq id }.mapNotNull { toEntity(it, listOf(stasjonAlias)) }
    }

    override fun toEntity(row: ResultRow, aliases: List<Alias<Table>>?): PlanlagtHenting {

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

        return PlanlagtHenting(
            row[table.id].value,
            row[table.startTidspunkt],
            row[table.sluttTidspunkt],
            //FIXME: GET FROM HENTEPLAN
            row[HenteplanTable.merknad],
            row[table.henteplanId],
            row[table.avlyst],
            row[table.avlystAv],
            row[table.aarsakId],
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
    }

    override fun update(
        params: PlanlagtHentingUpdateParams,
        avlystId: UUID
    ): Either<RepositoryError, PlanlagtHenting> {
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

    override fun updateAvlystDate(id: UUID, date: LocalDateTime, aarsak_Id: UUID, avlystAvId: UUID): Either<RepositoryError, PlanlagtHenting> {
        fun u(id: UUID, date: LocalDateTime, aarsak_Id: UUID, avlystAvId: UUID): Int {
            return table.update( {table.id eq id} ) { row ->
                row[avlyst] = date
                row[aarsakId] = aarsak_Id
                row[avlystAv] = avlystAvId
            }
        }
        return runCatching {
            u(id, date, aarsak_Id, avlystAvId)
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
            params.avlyst?.let {
                if (it) {row[avlyst] = LocalDateTime.now();}
                else {row[avlyst] = null; row[aarsakId] = null; row[avlystAv] = null}
            }
            params.aarsakId?.let { value ->
                if (params.avlyst != null && !params.avlyst!!)
                else row[aarsakId] = value
            }
        }
    }
}