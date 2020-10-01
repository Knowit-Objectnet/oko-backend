package no.oslokommune.ombruk.shared.utils.validation

import org.valiktor.Constraint
import org.valiktor.Validator


object NorwegianPhoneNumber : Constraint

fun <E> Validator<E>.Property<String?>.isNorwegianPhoneNumber() =
    this.validate(NorwegianPhoneNumber) {
        it == null || Regex("(0047|\\+47|47)?\\d{8}") matches it
    }