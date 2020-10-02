package no.oslokommune.ombruk.uttak.service

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import io.ktor.locations.KtorExperimentalLocationsAPI
import no.oslokommune.ombruk.uttak.database.UttakRepository
import no.oslokommune.ombruk.uttak.database.GjentakelsesRegels
import no.oslokommune.ombruk.uttak.form.UttakDeleteForm
import no.oslokommune.ombruk.uttak.form.UttakGetForm
import no.oslokommune.ombruk.uttak.form.UttakPostForm
import no.oslokommune.ombruk.uttak.form.UttakUpdateForm
import no.oslokommune.ombruk.uttak.model.Uttak
import no.oslokommune.ombruk.uttaksdata.service.UttaksdataService
import no.oslokommune.ombruk.shared.error.ServiceError
import org.jetbrains.exposed.sql.transactions.transaction

object UttakService : IUttakService {

    /**
     * Helper function for [saveUttak]. Takes an iterable [uttakPostForm] and posts every [Uttak] to the db.
     * This function is used for saving both singular and recurring uttak, leading to a slightly confusing name. With that
     * being said, both types of uttak are iterable, so the map on the [uttakPostForm] will only run once on singular uttak.
     *
     * @param uttakPostForm An [UttakPostForm] containing info on the [Uttak](s) to be stored.
     * @return a [ServiceError] on failure and the first stored [Uttak] on success.
     */
    private fun saveRecurring(uttakPostForm: UttakPostForm) = transaction {
        uttakPostForm.map { form ->
            UttakRepository.insertUttak(form)
                .flatMap { uttak ->
                    // Automatically generate a uttaksdata whenever an uttak is created.
                    UttaksdataService.saveReport(uttak).fold({ rollback(); it.left() }, { uttak.right() })
                }
        }.first()
    }

    override fun saveUttak(uttakPostForm: UttakPostForm): Either<ServiceError, Uttak> = transaction {
        let {
            uttakPostForm.gjentakelsesRegel?.let { GjentakelsesRegels.insertGjentakelsesRegel(it) } ?: Unit.right()
        }  // save recurrence rule, if set
            .flatMap { saveRecurring(uttakPostForm) }
            .fold({ rollback(); it.left() }, { it.right() })
    }

    override fun getUttakByID(id: Int): Either<ServiceError, Uttak> = transaction {
        UttakRepository.getUttakByID(id)
    }

    @KtorExperimentalLocationsAPI
    override fun getUttak(uttakGetForm: UttakGetForm?): Either<ServiceError, List<Uttak>> = transaction {
        UttakRepository.getUttak(uttakGetForm)
    }

    @KtorExperimentalLocationsAPI
    override fun deleteUttak(uttakDeleteForm: UttakDeleteForm): Either<ServiceError, List<Uttak>> = transaction {
        UttakRepository.deleteUttak(uttakDeleteForm)
    }

    override fun updateUttak(uttakUpdate: UttakUpdateForm): Either<ServiceError, Uttak> = transaction {
        UttakRepository.updateUttak(uttakUpdate)
            .flatMap { uttak ->
                UttaksdataService.updateReport(uttak)   // automatically update the uttaksdata. Rollback if this fails.
                    .fold({ rollback(); it.left() }, { uttak.right() })
            }
    }

}