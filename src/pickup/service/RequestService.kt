package ombruk.backend.pickup.service

import ombruk.backend.pickup.database.RequestRepository
import ombruk.backend.pickup.form.request.RequestDeleteForm
import ombruk.backend.pickup.form.request.RequestGetForm
import ombruk.backend.pickup.form.request.RequestPostForm

object RequestService : IRequestService {

    override fun saveRequest(requestPostForm: RequestPostForm) = RequestRepository.saveRequest(requestPostForm)

    override fun getRequests(requestGetForm: RequestGetForm?) = RequestRepository.getRequests(requestGetForm)

    override fun deleteRequest(requestDeleteForm: RequestDeleteForm) = RequestRepository.deleteRequest(requestDeleteForm)


}