package no.oslokommune.ombruk.shared.utils.validation

import arrow.core.extensions.list.functorFilter.filter
import org.valiktor.Constraint
import org.valiktor.ConstraintViolation
import org.valiktor.ConstraintViolationException


fun isAtLeastOnePropertySet(properties: List<Any?>) =
    if (properties.any { it != null }) { true } else {
        throw ConstraintViolationException(
            mutableSetOf(
                CustomConstraintViolation(AtLeastOnePropertySet(properties))
            ))
    }

class CustomConstraintViolation(override val constraint: Constraint) : ConstraintViolation {
    override val property: String = "All properties"
    override val value: String = "ConstraintViolation value"
}

data class AtLeastOnePropertySet(val properties: List<Any?>) : Constraint {
    override val messageParams: Map<String, *>
        get() = mapOf("properties" to null)
}
