package ombruk.backend.pickup.database

import arrow.core.Either
import ombruk.backend.pickup.form.request.RequestDeleteForm
import ombruk.backend.pickup.form.request.RequestGetForm
import ombruk.backend.pickup.form.request.RequestPostForm
import ombruk.backend.pickup.model.Request
import ombruk.backend.shared.error.RepositoryError

interface IRequestRepository {

    fun getRequests(requestGetForm: RequestGetForm?): Either<RepositoryError, List<Request>>
    fun saveRequest(requestPostForm: RequestPostForm): Either<RepositoryError, Request>
    fun deleteRequest(requestDeleteForm: RequestDeleteForm): Either<RepositoryError, Int>
}