package no.oslokommune.ombruk.uttak.service

import arrow.core.*
import arrow.core.extensions.either.foldable.fold
import arrow.core.extensions.either.monad.flatMap
import arrow.core.extensions.list.foldable.fold
import io.ktor.locations.KtorExperimentalLocationsAPI
import no.oslokommune.ombruk.shared.error.RepositoryError
import no.oslokommune.ombruk.uttak.database.UttakRepository
import no.oslokommune.ombruk.uttak.database.GjentakelsesRegelTable
import no.oslokommune.ombruk.uttak.form.UttakDeleteForm
import no.oslokommune.ombruk.uttak.form.UttakGetForm
import no.oslokommune.ombruk.uttak.form.UttakPostForm
import no.oslokommune.ombruk.uttak.form.UttakUpdateForm
import no.oslokommune.ombruk.uttak.model.Uttak
import no.oslokommune.ombruk.shared.error.ServiceError
import no.oslokommune.ombruk.uttak.model.GjentakelsesRegel
import no.oslokommune.ombruk.uttaksdata.database.UttaksDataRepository
import org.jetbrains.exposed.sql.transactions.transaction

object UttakService : IUttakService {

    // TODO: Return all created uttak
    override fun saveUttak(uttakPostForm: UttakPostForm): Either<ServiceError, Uttak> = transaction {
        uttakPostForm.gjentakelsesRegel?.let { gjenRegel ->

            // Generates forms using it.gjentakelsesRegel
            val generatedForms = uttakPostForm.map { form -> form }
            if (gjenRegel.sluttTidspunkt == null)
                gjenRegel.sluttTidspunkt = generatedForms.last().sluttTidspunkt
            if (gjenRegel.antall == null)
                gjenRegel.antall = generatedForms.size

            // Insert gjentakelsesregel and get id
            val id = GjentakelsesRegelTable.insertGjentakelsesRegel(gjenRegel)
                .map { gjRegel -> gjRegel.id!! }.getOrElse { -1 } // TODO: Dysfunctional

            // Update generated forms with id and insert them
            generatedForms.map { form ->
                form.gjentakelsesRegel!!.id = id
                UttakRepository.insertUttak(form)
            }.first()

        } ?: UttakRepository.insertUttak(uttakPostForm)
        .fold({ rollback(); it.left() }, { it.right() })
    }

    override fun getUttakByID(id: Int): Either<ServiceError, Uttak> = transaction {
        UttakRepository.getUttakByID(id)
    }

    override fun getUttak(uttakGetForm: UttakGetForm?): Either<ServiceError, List<Uttak>> = transaction {
        UttakRepository.getUttak(uttakGetForm)
    }

    override fun deleteUttak(uttak: Uttak): Either<ServiceError, Unit> = transaction {
        // GET Uttaksdata
        UttaksDataRepository.deleteByUttakId(uttak.id)
        UttakRepository.deleteUttak(uttak)
        .fold({ rollback(); it.left() }, { it.right() })
    }

    override fun deleteUttakById(id: Int): Either<ServiceError, Unit> = transaction {
        // GET Uttaksdata
        UttaksDataRepository.deleteByUttakId(id)
        UttakRepository.deleteUttakById(id)
            .fold({ rollback(); it.left() }, { it.right() })
    }

    fun deleteUttak(uttak: List<Uttak>): Either<ServiceError, Unit> = transaction {
        Either.cond(
            uttak.all { deleteUttak(it) is Either.Right },
            { Unit },
            { rollback(); ServiceError("aaa") } // TODO: Fix
        )
    }

    override fun updateUttak(uttakUpdate: UttakUpdateForm): Either<ServiceError, Uttak> = transaction {
        UttakRepository.updateUttak(uttakUpdate)
        .fold({ rollback(); it.left() }, { it.right() })
    }

}