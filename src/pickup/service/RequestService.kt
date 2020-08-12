package ombruk.backend.pickup.service

import arrow.core.Either
import ombruk.backend.pickup.database.RequestRepository
import ombruk.backend.pickup.form.request.RequestDeleteForm
import ombruk.backend.pickup.form.request.RequestGetForm
import ombruk.backend.pickup.form.request.RequestPostForm
import ombruk.backend.pickup.model.Request
import ombruk.backend.shared.error.ServiceError

object RequestService : IRequestService {

    override fun saveRequest(requestPostForm: RequestPostForm): Either<ServiceError, Request> =
        RequestRepository.saveRequest(requestPostForm)

    override fun getRequests(requestGetForm: RequestGetForm?): Either<ServiceError, List<Request>> =
        RequestRepository.getRequests(requestGetForm)

    override fun deleteRequest(requestDeleteForm: RequestDeleteForm): Either<ServiceError, Int> =
        RequestRepository.deleteRequest(requestDeleteForm)


}