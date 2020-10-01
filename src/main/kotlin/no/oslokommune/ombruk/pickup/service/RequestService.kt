package no.oslokommune.ombruk.pickup.service

import arrow.core.Either
import no.oslokommune.ombruk.pickup.database.RequestRepository
import no.oslokommune.ombruk.pickup.form.request.RequestDeleteForm
import no.oslokommune.ombruk.pickup.form.request.RequestGetForm
import no.oslokommune.ombruk.pickup.form.request.RequestPostForm
import no.oslokommune.ombruk.pickup.model.Request
import no.oslokommune.ombruk.shared.error.ServiceError

object RequestService : IRequestService {

    override fun saveRequest(requestPostForm: RequestPostForm): Either<ServiceError, Request> =
        RequestRepository.saveRequest(requestPostForm)

    override fun getRequests(requestGetForm: RequestGetForm?): Either<ServiceError, List<Request>> =
        RequestRepository.getRequests(requestGetForm)

    override fun deleteRequest(requestDeleteForm: RequestDeleteForm): Either<ServiceError, Int> =
        RequestRepository.deleteRequest(requestDeleteForm)


}