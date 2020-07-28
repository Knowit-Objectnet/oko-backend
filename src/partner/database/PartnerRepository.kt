package ombruk.backend.partner.database

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ombruk.backend.partner.form.PartnerForm
import ombruk.backend.partner.model.Partner
import ombruk.backend.shared.error.RepositoryError
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory


object Partners : IntIdTable("partners") {
    val name = varchar("name", 100)
}

object PartnerRepository : IPartnerRepository {
    private val logger = LoggerFactory.getLogger("ombruk.partner.service.PartnerRepository")

    override fun insertPartner(partner: PartnerForm) = runCatching {
        Partners.insertAndGetId {
            it[name] = partner.name
        }
    }
        .onFailure { logger.error("Failed to save partner to DB: ${it.message}") }
        .fold({ Partner(it.value, partner.name).right() }, {
            RepositoryError.InsertError(
                "SQL error"
            ).left()
        })


    override fun updatePartner(partner: PartnerForm) = runCatching {
        Partners.update({ Partners.id eq partner.id }) {
            it[name] = partner.name
        }
    }
        .onFailure { logger.error("Failed to update partner to DB: ${it.message}") }
        .fold(
            //Return right if more than 1 partner has been updated. Else, return an Error
            { Either.cond(it > 0, { Unit }, { RepositoryError.NoRowsFound("${partner.id} not found") }) },
            { RepositoryError.UpdateError(it.message).left() })


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
        Partners.select { Partners.id eq partnerID }.map {
            Partner(
                it[Partners.id].value,
                it[Partners.name]
            )
        }
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


    override fun getPartners(): Either<RepositoryError.SelectError, List<Partner>> =
        runCatching {
            Partners.selectAll().map {
                Partner(
                    it[Partners.id].value,
                    it[Partners.name]
                )
            }
        }
            .onFailure { logger.error(it.message) }
            .fold({ it.right() }, { RepositoryError.SelectError(it.message).left() })

    override fun exists(id: Int) = transaction { Partners.select { Partners.id eq id }.count() >= 1 }
}