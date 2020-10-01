package no.oslokommune.ombruk.shared.utils.validation

import no.oslokommune.ombruk.shared.database.IRepository
import org.valiktor.Constraint
import org.valiktor.Validator

data class ExistsInRepository<T : IRepository>(val repository: T) : Constraint

fun <E> Validator<E>.Property<Int?>.isInRepository(repository: IRepository) =
    this.validate(ExistsInRepository(repository)) {
        it == null || repository.exists(it)
    }