package no.oslokommune.ombruk.shared.utils.validation

import no.oslokommune.ombruk.shared.database.IRepositoryUniqueName
import org.valiktor.Constraint
import org.valiktor.Validator

data class IsUnique(val repository: IRepositoryUniqueName) : Constraint

fun <E> Validator<E>.Property<String?>.isUniqueInRepository(repository: IRepositoryUniqueName) =
    this.validate(IsUnique(repository)) {
        it == null || !repository.exists(it)
    }