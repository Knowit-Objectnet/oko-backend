package ombruk.backend.shared.utils.validators

import ombruk.backend.shared.database.IRepository
import org.valiktor.Constraint
import org.valiktor.Validator

data class ExistsInRepository<T : IRepository>(val repository: T) : Constraint

fun <E> Validator<E>.Property<Int?>.isInRepository(repository: IRepository) =
    this.validate(ExistsInRepository(repository)) {
        it == null || it == 0 || repository.exists(it)
    }