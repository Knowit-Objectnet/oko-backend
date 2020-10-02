package no.oslokommune.ombruk.uttak.database

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.ktor.locations.KtorExperimentalLocationsAPI
import no.oslokommune.ombruk.stasjon.database.Stasjoner
import no.oslokommune.ombruk.stasjon.database.toStasjon
import no.oslokommune.ombruk.uttak.form.UttakDeleteForm
import no.oslokommune.ombruk.uttak.form.UttakGetForm
import no.oslokommune.ombruk.uttak.form.UttakPostForm
import no.oslokommune.ombruk.uttak.form.UttakUpdateForm
import no.oslokommune.ombruk.uttak.model.Uttak
import no.oslokommune.ombruk.uttak.model.RecurrenceRule
import no.oslokommune.ombruk.uttak.model.toWeekDayList
import no.oslokommune.ombruk.partner.database.Partners
import no.oslokommune.ombruk.partner.database.toPartner
import no.oslokommune.ombruk.shared.error.RepositoryError
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

object UttakTable : IntIdTable("uttak") {
    val startDateTime = datetime("start_date_time")
    val endDateTime = datetime("end_date_time")
    val recurrenceRuleID =
        integer("recurrence_rule_id").references(RecurrenceRules.id, onDelete = ReferenceOption.CASCADE).nullable()
    val stasjonID = integer("stasjon_id").references(Stasjoner.id, onDelete = ReferenceOption.CASCADE)
    // Nullable partner. An uttak without a partner is arranged by the stasjon only, like example "Ombruksdager".
    val partnerID = integer("partner_id").references(Partners.id, onDelete = ReferenceOption.CASCADE).nullable()
}

object UttakRepository : IUttakRepository {
    private val logger = LoggerFactory.getLogger("ombruk.backend.service.UttakRepository")

    override fun insertUttak(uttakPostForm: UttakPostForm): Either<RepositoryError, Uttak> = runCatching {
        UttakTable.insertAndGetId {
            it[startDateTime] = uttakPostForm.startDateTime
            it[endDateTime] = uttakPostForm.endDateTime
            it[recurrenceRuleID] = uttakPostForm.recurrenceRule?.id
            it[stasjonID] = uttakPostForm.stasjonId
            it[partnerID] = uttakPostForm.partnerId
        }.value
    }
        .onFailure { logger.error("Failed to save uttak to DB; ${it.message}") }
        .fold(
            { getUttakByID(it) },
            { RepositoryError.InsertError("SQL error").left() }
        )


    override fun updateUttak(uttak: UttakUpdateForm): Either<RepositoryError, Uttak> = runCatching {
        transaction {
            UttakTable.update({ UttakTable.id eq uttak.id }) { row ->
                uttak.startDateTime?.let { row[startDateTime] = uttak.startDateTime }
                uttak.endDateTime?.let { row[endDateTime] = uttak.endDateTime }
            }
        }
    }
        .onFailure { logger.error(it.message) }
        .fold(
            { getUttakByID(uttak.id) },
            { RepositoryError.UpdateError(it.message).left() }
        )


    @KtorExperimentalLocationsAPI
    override fun deleteUttak(uttakDeleteForm: UttakDeleteForm): Either<RepositoryError, List<Uttak>> =
        runCatching {
            transaction {
                /*
                This is a conditional delete, and is somewhat special. Essentially, what's being done is building separate
                operations for each value of the uttakDeleteForm that's not null. These are then added to a list, which is
                then combined to a full statement that can be ran. Op.build is a bit finicky as to what it accepts
                as input, so you might have to use function (foo.lessEq(bar)) instead of DSL (foo eq bar) some places.
                 */
                val statements = mutableListOf<Op<Boolean>>()
                uttakDeleteForm.uttakId?.let { statements.add(Op.build { UttakTable.id eq it }) }
                uttakDeleteForm.recurrenceRuleId?.let { statements.add(Op.build { UttakTable.recurrenceRuleID eq it }) }
                uttakDeleteForm.fromDate?.let { statements.add(Op.build { UttakTable.startDateTime.greaterEq(it) }) }
                uttakDeleteForm.toDate?.let { statements.add(Op.build { UttakTable.startDateTime.lessEq(it) }) }
                uttakDeleteForm.partnerId?.let { statements.add(Op.build { UttakTable.partnerID eq it }) }
                uttakDeleteForm.stasjonId?.let { statements.add(Op.build { UttakTable.stasjonID eq it }) }

                // Delete and return deleted uttaks. Have to handle the case where no statements are sepcified
                if (statements.isEmpty()) {
                    val result = (UttakTable innerJoin Stasjoner innerJoin Partners leftJoin RecurrenceRules).selectAll()
                        .mapNotNull { toUttak(it) }
                    UttakTable.deleteAll()
                    return@transaction result
                } else {
                    val statement = AndOp(statements)
                    val result =
                        (UttakTable innerJoin Stasjoner innerJoin Partners leftJoin RecurrenceRules).select { statement }
                            .mapNotNull { toUttak(it) }
                    UttakTable.deleteWhere { statement }
                    return@transaction result
                }
            }
        }
            .onFailure { logger.error(it.message) }
            .fold(
                { Either.cond(it.isNotEmpty(), { it }, { RepositoryError.NoRowsFound("No matches found") }) },
                { RepositoryError.DeleteError(it.message).left() })


    override fun getUttakByID(uttakID: Int): Either<RepositoryError, Uttak> = runCatching {
        transaction {
            // leftJoin RecurrenceRules because not all uttaks are recurring.
            (UttakTable innerJoin Stasjoner leftJoin Partners leftJoin RecurrenceRules).select { UttakTable.id eq uttakID }
                .map { toUttak(it) }.firstOrNull()
        }
    }
        .onFailure { logger.error(it.message) }
        .fold(
            { Either.cond(it != null, { it!! }, { RepositoryError.NoRowsFound("ID does not exist!") }) },
            { RepositoryError.SelectError(it.message).left() })


    @KtorExperimentalLocationsAPI
    override fun getUttaks(uttakGetForm: UttakGetForm?): Either<RepositoryError, List<Uttak>> =
        runCatching {
            transaction {
                val query = (UttakTable innerJoin Stasjoner innerJoin Partners leftJoin RecurrenceRules).selectAll()
                if (uttakGetForm != null) {
                    uttakGetForm.uttakId?.let { query.andWhere { UttakTable.id eq it } }
                    uttakGetForm.stasjonId?.let { query.andWhere { UttakTable.stasjonID eq it } }
                    uttakGetForm.partnerId?.let { query.andWhere { UttakTable.partnerID eq it } }
                    uttakGetForm.recurrenceRuleId?.let { query.andWhere { UttakTable.recurrenceRuleID eq it } }
                    uttakGetForm.fromDate?.let { query.andWhere { UttakTable.startDateTime.greaterEq(it) } }
                    uttakGetForm.toDate?.let { query.andWhere { UttakTable.endDateTime.lessEq(it) } }
                }
                query.mapNotNull { toUttak(it) }
            }
        }
            .onFailure { logger.error(it.message) }
            .fold(
                { it.right() },
                { RepositoryError.SelectError(it.message).left() }
            )

    override fun exists(id: Int) = transaction { UttakTable.select { UttakTable.id eq id }.count() >= 1 }


    private fun toUttak(row: ResultRow?): Uttak? {
        if (row == null) return null

        return Uttak(
            row[UttakTable.id].value,
            row[UttakTable.startDateTime],
            row[UttakTable.endDateTime],
            toStasjon(row),
            toPartner(row),
            getGetRecurrenceRuleFromResultRow(row)
        )
    }


    private fun getGetRecurrenceRuleFromResultRow(row: ResultRow): RecurrenceRule? {
        if (!row.hasValue(RecurrenceRules.id) || row.getOrNull(
                RecurrenceRules.id
            ) == null
        ) return null

        return RecurrenceRule(
            row[RecurrenceRules.id].value,
            row[RecurrenceRules.until],
            row[RecurrenceRules.days]?.toWeekDayList(),
            row[RecurrenceRules.interval],
            row[RecurrenceRules.count]
        )
    }
}