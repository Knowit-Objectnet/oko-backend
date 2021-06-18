package ombruk.backend.shared.utils.validation

import org.valiktor.Constraint
import org.valiktor.Validator
import java.util.*

object UUIDString : Constraint

fun <E> Validator<E>.Property<String?>.isLegalUUID() =
    this.validate(UUIDString) {
        try {
            UUID.fromString(it)
            true
        } catch (e: Exception) {
            false
        }
    }

object UUIDStringList : Constraint

fun <E> Validator<E>.Property<Iterable<String>?>.allUUIDLegal() =
    this.validate(UUIDStringList) {
        try {
            it?.map { UUID.fromString(it) }
            true
        } catch (e: Exception) {
            false
        }
    }