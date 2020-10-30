package no.oslokommune.ombruk.uttak.service

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.ktor.locations.KtorExperimentalLocationsAPI
import no.oslokommune.ombruk.uttak.database.UttakRepository
import no.oslokommune.ombruk.uttak.database.GjentakelsesRegelTable
import no.oslokommune.ombruk.uttak.form.UttakDeleteForm
import no.oslokommune.ombruk.uttak.form.UttakGetForm
import no.oslokommune.ombruk.uttak.form.UttakPostForm
import no.oslokommune.ombruk.uttak.form.UttakUpdateForm
import no.oslokommune.ombruk.uttak.model.Uttak
import no.oslokommune.ombruk.shared.error.ServiceError
import org.jetbrains.exposed.sql.transactions.transaction

object UttakService : IUttakService {

    private fun saveRecurring(uttakPostForm: UttakPostForm) = transaction {
        uttakPostForm.map { form ->
            UttakRepository.insertUttak(form)
        }.first()
            .fold({ rollback(); it.left() }, { it.right() })
    }

    override fun saveUttak(uttakPostForm: UttakPostForm): Either<ServiceError, Uttak> = transaction {
        let {
            uttakPostForm.gjentakelsesRegel?.let { GjentakelsesRegelTable.insertGjentakelsesRegel(it) } ?: Unit.right()
            UttakRepository.insertUttak(uttakPostForm)
        }  // save recurrence rule, if set
            .fold({ rollback(); it.left() }, { it.right() })
    }

    /*
    override fun saveUttak(uttakPostForm: UttakPostForm): Either<ServiceError, Uttak> = transaction {
        let {
            uttakPostForm.gjentakelsesRegel?.let { GjentakelsesRegels.insertGjentakelsesRegel(it) } ?: Unit.right()
        }  // save recurrence rule, if set
            .flatMap { saveRecurring(uttakPostForm) }
            .fold({ rollback(); it.left() }, { it.right() })
    }

     */

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
        .fold({ rollback(); it.left() }, { it.right() })
    }

    override fun updateUttak(uttakUpdate: UttakUpdateForm): Either<ServiceError, Uttak> = transaction {
        UttakRepository.updateUttak(uttakUpdate)
        .fold({ rollback(); it.left() }, { it.right() })
    }

}