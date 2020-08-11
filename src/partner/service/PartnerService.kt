package ombruk.backend.partner.service

import arrow.core.Either
import arrow.core.extensions.either.monad.flatMap
import arrow.core.left
import arrow.core.right
import com.typesafe.config.ConfigFactory
import io.ktor.config.HoconApplicationConfig
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.util.KtorExperimentalAPI
import ombruk.backend.partner.database.PartnerRepository
import ombruk.backend.partner.form.PartnerGetForm
import ombruk.backend.partner.form.PartnerPostForm
import ombruk.backend.partner.form.PartnerUpdateForm
import ombruk.backend.partner.model.Partner
import ombruk.backend.shared.api.KeycloakGroupIntegration
import ombruk.backend.shared.error.ServiceError
import org.jetbrains.exposed.sql.transactions.transaction

object PartnerService : IPartnerService {

    @KtorExperimentalAPI
    private val appConfig = HoconApplicationConfig(ConfigFactory.load())

    @KtorExperimentalAPI
    private val isDebug: Boolean = appConfig.property("ktor.oko.debug").getString().toBoolean()

    @KtorExperimentalAPI
    override fun savePartner(partnerForm: PartnerPostForm) = transaction {
        val partner = PartnerRepository.insertPartner(partnerForm)
            .fold(
                { return@transaction it.left() },
                { it })     //return immediately if left, cast value to right if possible.

        takeIf { !isDebug }?.let {  //only post to keycloak if not debugging.
            KeycloakGroupIntegration.createGroup(partner.name, partner.id)
                .bimap({ rollback(); it }, { partner })     //rollback entire transaction if keycloak fails.
        } ?: partner.right()    //return partner if debugging.
    }

    override fun getPartnerById(id: Int) = PartnerRepository.getPartnerByID(id)

    @KtorExperimentalLocationsAPI
    override fun getPartners(partnerGetForm: PartnerGetForm) = PartnerRepository.getPartners(partnerGetForm)

    @KtorExperimentalAPI
    override fun deletePartnerById(id: Int): Either<ServiceError, Unit> = transaction {
        val partner = getPartnerById(id)
            .fold({ return@transaction it.left() }, { it })

        takeIf { !isDebug }?.let {
            PartnerRepository.deletePartner(id)
                .flatMap { KeycloakGroupIntegration.deleteGroup(partner.name) }
                .bimap({ rollback(); it }, { Unit })
        } ?: PartnerRepository.deletePartner(id)
    }


    @KtorExperimentalAPI
    override fun updatePartner(partnerForm: PartnerUpdateForm): Either<ServiceError, Partner> = transaction {
        val partner = getPartnerById(partnerForm.id)
            .fold({ return@transaction it.left() }, { it })

        //Keycloak only needs to be updated when not in debug AND the name of a partner needs to be updated.
        takeIf { isDebug || partnerForm.name == null }?.let { PartnerRepository.updatePartner(partnerForm) }
            ?: PartnerRepository.updatePartner(partnerForm)
                .flatMap { newPartner ->
                    KeycloakGroupIntegration.updateGroup(partner.name, newPartner.name)
                        .bimap({ rollback(); it }, { newPartner })
                }
    }
}