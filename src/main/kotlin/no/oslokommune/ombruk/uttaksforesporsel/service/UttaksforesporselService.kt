package no.oslokommune.ombruk.uttaksforesporsel.service

import arrow.core.Either
import no.oslokommune.ombruk.uttaksforesporsel.database.UttaksforesporselRepository
import no.oslokommune.ombruk.uttaksforesporsel.form.uttaksforesporsel.UttaksforesporselDeleteForm
import no.oslokommune.ombruk.uttaksforesporsel.form.uttaksforesporsel.UttaksforesporselGetForm
import no.oslokommune.ombruk.uttaksforesporsel.form.uttaksforesporsel.UttaksforesporselPostForm
import no.oslokommune.ombruk.uttaksforesporsel.model.Uttaksforesporsel
import no.oslokommune.ombruk.shared.error.ServiceError

object UttaksforesporselService : IUttaksforesporselService {

    override fun saveRequest(requestPostForm: UttaksforesporselPostForm): Either<ServiceError, Uttaksforesporsel> =
        UttaksforesporselRepository.saveRequest(requestPostForm)

    override fun getRequests(requestGetForm: UttaksforesporselGetForm?): Either<ServiceError, List<Uttaksforesporsel>> =
        UttaksforesporselRepository.getRequests(requestGetForm)

    override fun deleteRequest(requestDeleteForm: UttaksforesporselDeleteForm): Either<ServiceError, Int> =
        UttaksforesporselRepository.deleteRequest(requestDeleteForm)


}