package ombruk.backend.database

import org.jetbrains.exposed.sql.Table

object Requests : Table("requests") {
    val pickupID = integer("pickup_id").references(Pickups.id)
    val partnerID = integer("partner_id").references(Partners.id)

    override val primaryKey = PrimaryKey(pickupID, partnerID, name = "id")
}
