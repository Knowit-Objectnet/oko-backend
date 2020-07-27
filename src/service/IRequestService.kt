package ombruk.backend.service

import ombruk.backend.model.Request


interface IRequestService {
    fun addPartnersToPickup(data: Request)
    fun getRequests(pickupID: Int?, partnerID: Int?): List<Request>
    fun deleteRequests(pickupID: Int?, partnerID: Int?, stationID: Int?): Boolean
}