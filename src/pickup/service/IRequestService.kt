package ombruk.backend.pickup.service

import ombruk.backend.pickup.model.Request


interface IRequestService {
    fun addPartnersToPickup(data: Request)
    fun getRequests(pickupID: Int?, partnerID: Int?): List<Request>
    fun deleteRequests(pickupID: Int?, partnerID: Int?, stationID: Int?): Boolean
}