package no.oslokommune.ombruk.uttaksforesporsel.service

import arrow.core.Either
import no.oslokommune.ombruk.uttaksforesporsel.database.UttaksforesporselRepository
import no.oslokommune.ombruk.uttaksforesporsel.form.uttaksforesporsel.UttaksforesporselDeleteForm
import no.oslokommune.ombruk.uttaksforesporsel.form.uttaksforesporsel.UttaksForesporselGetForm
import no.oslokommune.ombruk.uttaksforesporsel.form.uttaksforesporsel.UttaksforesporselPostForm
import no.oslokommune.ombruk.uttaksforesporsel.model.UttaksForesporsel
import no.oslokommune.ombruk.shared.error.ServiceError

object UttaksforesporselService : IUttaksforesporselService {

    override fun saveForesporsel(foresporselPostForm: UttaksforesporselPostForm): Either<ServiceError, UttaksForesporsel> =
        UttaksforesporselRepository.saveForesporsel(foresporselPostForm)

    override fun getForesporsler(foresporselGetForm: UttaksForesporselGetForm?): Either<ServiceError, List<UttaksForesporsel>> =
        UttaksforesporselRepository.getForesporsler(foresporselGetForm)

    override fun deleteForesporsel(foresporselDeleteForm: UttaksforesporselDeleteForm): Either<ServiceError, Int> =
        UttaksforesporselRepository.deleteForesporsel(foresporselDeleteForm)


}