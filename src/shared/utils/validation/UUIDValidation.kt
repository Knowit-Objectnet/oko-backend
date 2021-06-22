package ombruk.backend.shared.utils.validation

import ombruk.backend.kategori.application.api.dto.EkstraHentingKategoriBatchSaveDto
import ombruk.backend.kategori.application.api.dto.HenteplanKategoriBatchSaveDto
import ombruk.backend.kategori.application.api.dto.IKategoriKoblingSaveDto
import org.valiktor.Constraint
import org.valiktor.Validator
import java.util.*

object UUIDString : Constraint

/**
 * Takes a [String] and checks if they are valid [UUID]s. If a function is passed, it will validate that
 * it returns true for the id.
 */
fun <E> Validator<E>.Property<String?>.isLegalUUID(function: ((UUID) -> Boolean) = {true}) =
    this.validate(UUIDString) {
        try {
            UUID.fromString(it)
                .let { function(it) }
        } catch (e: Exception) {
            false
        }
    }

object UUIDStringList : Constraint

/**
 * Takes a list of [String]s and checks if they are valid [UUID]s. If a function is passed, it will validate that
 * it returns true for each id.
 */
fun <E> Validator<E>.Property<Iterable<String>?>.allUUIDLegal(function: ((UUID) -> Boolean) = {true}) =
    this.validate(UUIDStringList) { stringList ->
        try {
            val allValid = stringList?.map { UUID.fromString(it) }
                ?.map { function(it)}
                ?.all { it }
            allValid ?: false
        } catch (e: Exception) {
            false
        }
    }

// TODO: Make generic, for types implementing interface IKategoriKoblingSaveDto
fun <E> Validator<E>.Property<Iterable<HenteplanKategoriBatchSaveDto>?>.allValidUUIDHenteplan(function: ((UUID) -> Boolean) = {true}) =
    this.validate(UUIDStringList) { dtoList ->
        try {
            val allValid = dtoList?.map { it.kategoriId }
                ?.map { function(it)}
                ?.all { it }
            allValid ?: false
        } catch (e: Exception) {
            false
        }
    }
fun <E> Validator<E>.Property<Iterable<EkstraHentingKategoriBatchSaveDto>?>.allValidUUIDEkstraHenting(function: ((UUID) -> Boolean) = {true}) =
    this.validate(UUIDStringList) { dtoList ->
        try {
            val allValid = dtoList?.map { it.kategoriId }
                ?.map { function(it)}
                ?.all { it }
            allValid ?: false
        } catch (e: Exception) {
            false
        }
    }