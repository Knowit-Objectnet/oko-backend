package ombruk.backend.partner.database

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import ombruk.backend.partner.form.PartnerGetForm

import ombruk.backend.partner.form.PartnerPostForm
import ombruk.backend.partner.form.PartnerUpdateForm
import ombruk.backend.partner.model.Partner
import ombruk.backend.shared.error.RepositoryError
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory


object Partners : IntIdTable("partners") {
    val name = varchar("name", 100)
    val description = varchar("description", 100)
    val phone = varchar("phone", 20)
    val email = varchar("email", 30)
}

object PartnerRepository : IPartnerRepository {
    private val logger = LoggerFactory.getLogger("ombruk.partner.service.PartnerRepository")

    override fun insertPartner(partner: PartnerPostForm) = runCatching {
        Partners.insertAndGetId {
            it[name] = partner.name
            it[description] = partner.description
            it[phone] = partner.phone
            it[email] = partner.email
        }
    }
        .onFailure { logger.error("Failed to save partner to DB: ${it.message}") }
        .fold(
            { Partner(it.value, partner.name, partner.description, partner.phone, partner.email).right() },
            { RepositoryError.InsertError("SQL error").left() }
        )


    override fun updatePartner(partner: PartnerUpdateForm) = runCatching {
        Partners.update({ Partners.id eq partner.id }) { row ->
            partner.name?.let { row[name] = it }
            partner.description?.let { row[description] = it }
            partner.phone?.let { row[phone] = it }
            partner.email?.let { row[email] = it }
        }
    }
        .onFailure { logger.error("Failed to update partner to DB: ${it.message}") }
        .fold(
            //Return right if more than 1 partner has been updated. Else, return an Error
            {
                Either.cond(
                    it > 0,
                    { getPartnerByID(partner.id) },
                    { RepositoryError.NoRowsFound("${partner.id} not found") })
            },
            { RepositoryError.UpdateError(it.message).left() })
        .flatMap { it }


    override fun deletePartner(partnerID: Int) =
        runCatching { Partners.deleteWhere { Partners.id eq partnerID } }
            .onFailure { logger.error("Failed to delete partner in DB: ${it.message}") }
            .fold(
                {
                    Either.cond(it > 0, { Unit }) {
                        RepositoryError.NoRowsFound(
                            "$partnerID not found"
                        )
                    }
                },
                { RepositoryError.DeleteError(it.message).left() })


    override fun getPartnerByID(partnerID: Int): Either<RepositoryError.NoRowsFound, Partner> = runCatching {
        Partners.select { Partners.id eq partnerID }.map { toPartner(it) }
    }
        .onFailure { logger.error(it.message) }
        .fold(
            {
                Either.cond(it.isNotEmpty(), { it.first() }, {
                    RepositoryError.NoRowsFound(
                        "$partnerID not found"
                    )
                })
            },
            { RepositoryError.NoRowsFound(it.message).left() })


    override fun getPartners(partnerGetForm: PartnerGetForm): Either<RepositoryError.SelectError, List<Partner>> =
        runCatching {
            val query = Partners.selectAll()
            partnerGetForm.name?.let { query.andWhere { Partners.name eq it } }
            query.mapNotNull { toPartner(it) }
        }
            .onFailure { logger.error(it.message) }
            .fold({ it.right() }, { RepositoryError.SelectError(it.message).left() })

    override fun exists(id: Int) = transaction { Partners.select { Partners.id eq id }.count() >= 1 }
}


fun toPartner(resultRow: ResultRow): Partner =
    Partner(
        resultRow[Partners.id].value,
        resultRow[Partners.name],
        resultRow[Partners.description],
        resultRow[Partners.phone],
        resultRow[Partners.email]
    )
