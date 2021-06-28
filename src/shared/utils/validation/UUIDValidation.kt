package ombruk.backend.shared.utils.validation

import arrow.core.Either
import arrow.core.extensions.either.foldable.isEmpty
import arrow.core.right
import ombruk.backend.aktor.application.api.dto.PartnerGetDto
import ombruk.backend.aktor.application.api.dto.StasjonFindDto
import ombruk.backend.aktor.application.service.IPartnerService
import ombruk.backend.aktor.application.service.IStasjonService
import ombruk.backend.kategori.application.api.dto.EkstraHentingKategoriBatchSaveDto
import ombruk.backend.kategori.application.api.dto.HenteplanKategoriBatchSaveDto
import ombruk.backend.kategori.application.api.dto.IKategoriKoblingSaveDto
import org.valiktor.Constraint
import org.valiktor.Validator
import org.valiktor.constraints.Valid
import org.valiktor.functions.isValid
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


object UUIDKategori : Constraint
object UUIDHenteplan: Constraint
object UUIDGenerelt: Constraint

fun <E, UUID> Validator<E>.Property<UUID?>.isExistingUUID(validator: ((UUID) -> Boolean), type: Constraint): Validator<E>.Property<UUID?> {
    return when (type) {
        is UUIDKategori -> this.validate(UUIDKategori) { it == null || validator(it) }
        is UUIDHenteplan -> this.validate(UUIDHenteplan) { it == null || validator(it) }
        else -> { // Note the block
            this.validate(UUIDGenerelt) { it == null || validator(it) }
        }
    }
}

object UniqueNavn: Constraint

fun <E> Validator<E>.Property<String?>.isUniqueNavn(partnerService: IPartnerService, stasjonService: IStasjonService): Validator<E>.Property<String?> {
    return this.validate(UniqueNavn) { navn ->
        navn == null || run {
            val partnerList = partnerService.getPartnere(PartnerGetDto(navn = navn), false)
            val stasjonList = stasjonService.find(StasjonFindDto(navn = navn), false)
            partnerList.exists { it.isEmpty() } && stasjonList.exists { it.isEmpty() }
        }
    }
}


