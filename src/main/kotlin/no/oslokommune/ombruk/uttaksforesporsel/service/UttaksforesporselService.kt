package no.oslokommune.ombruk.uttaksforesporsel.service

import arrow.core.Either
import no.oslokommune.ombruk.uttaksforesporsel.database.UttaksforesporselRepository
import no.oslokommune.ombruk.uttaksforesporsel.form.uttaksforesporsel.UttaksforesporselDeleteForm
import no.oslokommune.ombruk.uttaksforesporsel.form.uttaksforesporsel.UttaksForesporselGetForm
import no.oslokommune.ombruk.uttaksforesporsel.form.uttaksforesporsel.UttaksforesporselPostForm
import no.oslokommune.ombruk.uttaksforesporsel.model.UttaksForesporsel
import no.oslokommune.ombruk.shared.error.ServiceError

object UttaksforesporselService : IUttaksforesporselService {

    override fun saveRequest(requestPostForm: UttaksforesporselPostForm): Either<ServiceError, UttaksForesporsel> =
        UttaksforesporselRepository.saveForesporsel(requestPostForm)

    override fun getRequests(requestGetForm: UttaksForesporselGetForm?): Either<ServiceError, List<UttaksForesporsel>> =
        UttaksforesporselRepository.getForesporsler(requestGetForm)

    override fun deleteRequest(requestDeleteForm: UttaksforesporselDeleteForm): Either<ServiceError, Int> =
        UttaksforesporselRepository.deleteForesporsel(requestDeleteForm)


}