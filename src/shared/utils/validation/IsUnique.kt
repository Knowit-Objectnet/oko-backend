package ombruk.backend.shared.utils.validation

import ombruk.backend.aktor.application.api.dto.PartnerGetDto
import ombruk.backend.aktor.application.api.dto.StasjonFindDto
import ombruk.backend.aktor.application.service.IPartnerService
import ombruk.backend.aktor.application.service.IStasjonService
import org.valiktor.Constraint
import org.valiktor.Validator
import java.util.*

object UniqueNavn: Constraint

/**
 * Takes a [String] and checks if they are unique in the database. If a function is passed, it will validate that
 * it returns true for the navn.
 */
fun <E> Validator<E>.Property<String?>.isUniqueNavn(id: UUID?, partnerService: IPartnerService, stasjonService: IStasjonService): Validator<E>.Property<String?> {
    return this.validate(UniqueNavn) { navn ->
        navn == null || run {
            if (id != null) (partnerService.getPartnerById(id, false).exists { it.navn == navn } || stasjonService.findOne(id, false).exists { it.navn == navn })
            else {
            val partnerList = partnerService.getPartnere(PartnerGetDto(navn = navn), false)
            val stasjonList = stasjonService.find(StasjonFindDto(navn = navn), false)
            partnerList.exists { it.isEmpty() } && stasjonList.exists { it.isEmpty() }
            }
        }
    }
}


