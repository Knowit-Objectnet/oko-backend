package ombruk.backend.service

import ombruk.backend.database.Partners
import ombruk.backend.database.Pickups
import ombruk.backend.database.Requests
import ombruk.backend.model.Partner
import ombruk.backend.model.Request
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object RequestService : IRequestService {
    override fun addPartnersToPickup(data: Request){
        val check = getRequests(data.pickupID, data.partner.id)
        if (check.isNotEmpty()){
            throw IllegalArgumentException("This request already exists")
        }
        return transaction{
            Requests.insert {
                it[pickupID] = data.pickupID
                it[partnerID] = data.partner.id
            }

        }
    }

    override fun getRequests(pickupID: Int?, partnerID: Int?): List<Request>{
        return transaction {
            val query = (Requests innerJoin Partners).selectAll()
            pickupID?.let { query.andWhere { Requests.pickupID eq it } }
            partnerID?.let { query.andWhere { Requests.partnerID eq it } }
            query.map { toRequest(it) }
        }
    }

    override fun deleteRequests (pickupID: Int?, partnerID: Int?, stationID: Int?):Boolean{
        if (pickupID == null && partnerID == null && stationID == null){
            throw IllegalArgumentException("Must set a parameter")
        }
        val query = (Partners innerJoin Requests innerJoin Pickups).selectAll()
        pickupID?.let { query.andWhere { Pickups.id eq pickupID } }
        stationID?.let { query.andWhere { Pickups.stationID eq stationID } }
        partnerID?.let { query.andWhere { Requests.partnerID eq partnerID } }
        var count = 0
        return try{
            transaction {
                // TODO optimize
                query.forEach { count += Requests.deleteWhere { Requests.pickupID eq it[Pickups.id].value and
                        (Requests.partnerID eq it[Requests.partnerID]) } }
            }
            count>0
        } catch(e: Exception){
            false
        }
    }

    private fun toRequest(row: ResultRow): Request{
        return Request(
            row[Requests.pickupID],
            Partner(row[Partners.id].value, row[Partners.name])
        )
    }
}