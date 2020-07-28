package ombruk.backend.partner.service

import arrow.core.Either
import arrow.core.extensions.either.monad.flatMap
import arrow.core.left
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import ombruk.backend.partner.database.PartnerRepository
import ombruk.backend.partner.form.PartnerPostForm
import ombruk.backend.partner.form.PartnerUpdateForm
import ombruk.backend.shared.error.ServiceError
import org.jetbrains.exposed.sql.transactions.transaction

class PartnerService : IPartnerService {
    private val json = Json(JsonConfiguration.Stable)

    override fun savePartner(partnerForm: PartnerPostForm) = transaction {
        when (val partner = PartnerRepository.insertPartner(partnerForm)) {
            is Either.Left -> partner.a.left()
            is Either.Right -> {
                KeycloakGroupIntegration.createGroup(partnerForm.name, partner.b.id)
                    .bimap({ rollback(); it }, { partner.b })
            }
        }

    }


    override fun getPartnerById(id: Int) = transaction { PartnerRepository.getPartnerByID(id) }


    override fun getPartners() = transaction { PartnerRepository.getPartners() }


    override fun deletePartnerById(id: Int): Either<ServiceError, Unit> = transaction {
        val partner = getPartnerById(id)
            .fold({ return@transaction it.left() }, { it })

        PartnerRepository.deletePartner(id)
            .flatMap { KeycloakGroupIntegration.deleteGroup(partner.name) }
            .bimap({ rollback(); it }, { Unit })
    }


    override fun updatePartner(partnerForm: PartnerUpdateForm): Either<ServiceError, Unit> = transaction {
        when (val partner = getPartnerById(partnerForm.id)) {
            is Either.Left -> partner.a.left()
            is Either.Right -> {
                PartnerRepository.updatePartner(partnerForm)
                    .map {partnerForm.name?.let { KeycloakGroupIntegration.updateGroup(partner.b.name, partnerForm.name!!) }}
                    .bimap({ rollback(); it }, { Unit })
            }
        }
    }
}