package ombruk.backend.pickup.service

import arrow.core.Either
import ombruk.backend.pickup.form.request.RequestDeleteForm
import ombruk.backend.pickup.form.request.RequestGetForm
import ombruk.backend.pickup.form.request.RequestPostForm
import ombruk.backend.pickup.model.Request
import ombruk.backend.shared.error.ServiceError


interface IRequestService {
    fun saveRequest(requestPostForm: RequestPostForm): Either<ServiceError, Request>
    fun getRequests(requestGetForm: RequestGetForm): Either<ServiceError, List<Request>>
    fun deleteRequest(requestDeleteForm: RequestDeleteForm): Either<ServiceError, Int>
}