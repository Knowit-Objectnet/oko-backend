package ombruk.backend.calendar.database

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ombruk.backend.calendar.model.RecurrenceRule
import ombruk.backend.shared.error.RepositoryError
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.jetbrains.exposed.sql.insertAndGetId
import org.slf4j.LoggerFactory

object RecurrenceRules : IntIdTable("recurrence_rules") {
    val days = varchar("days", 50).nullable()
    val count = integer("count").nullable()
    val until = datetime("until").nullable()
    val interval = integer("\"interval\"")

    private val logger = LoggerFactory.getLogger("ombruk.backend.service.PartnerRepository")

    fun insertRecurrenceRule(rRule: RecurrenceRule): Either<RepositoryError, RecurrenceRule> = runCatching {
        RecurrenceRules.insertAndGetId {
            it[days] = rRule.days?.joinToString()
            it[count] = rRule.count
            it[until] = rRule.until
            it[interval] = rRule.interval
        }
    }
        .onFailure { logger.error(it.message) }
        .fold(
            {
                rRule.id = it.value;
                RecurrenceRule(
                    it.value,
                    rRule.until,
                    rRule.days,
                    rRule.interval,
                    rRule.count
                ).right()
            },
            { RepositoryError.InsertError(it.message).left() })
}