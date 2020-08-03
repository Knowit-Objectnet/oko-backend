package ombruk.backend.partner.service

import arrow.core.Either
import arrow.core.extensions.either.monad.flatMap
import arrow.core.left
import arrow.core.right
import com.typesafe.config.ConfigFactory
import io.ktor.config.HoconApplicationConfig
import io.ktor.util.KtorExperimentalAPI
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import ombruk.backend.partner.database.PartnerRepository
import ombruk.backend.partner.form.PartnerGetForm
import ombruk.backend.partner.form.PartnerPostForm
import ombruk.backend.partner.form.PartnerUpdateForm
import ombruk.backend.partner.model.Partner
import ombruk.backend.shared.error.ServiceError
import org.jetbrains.exposed.sql.transactions.transaction

class PartnerService : IPartnerService {
    private val json = Json(JsonConfiguration.Stable)

    private val appConfig = HoconApplicationConfig(ConfigFactory.load())
    private val isDebug: Boolean = appConfig.property("ktor.oko.debug").getString().toBoolean()

    override fun savePartner(partnerForm: PartnerPostForm) = transaction {
        when (val partner = PartnerRepository.insertPartner(partnerForm)) {
            is Either.Left -> partner.a.left()
            is Either.Right -> {
                when (isDebug) {
                    true -> partner.b.right()
                    else -> KeycloakGroupIntegration.createGroup(partnerForm.name, partner.b.id)
                        .bimap({ rollback(); it }, { partner.b })
                }
            }
        }
    }


    override fun getPartnerById(id: Int) = transaction { PartnerRepository.getPartnerByID(id) }


    override fun getPartners(partnerGetForm: PartnerGetForm) =
        transaction { PartnerRepository.getPartners(partnerGetForm) }


    @KtorExperimentalAPI
    override fun deletePartnerById(id: Int): Either<ServiceError, Unit> = transaction {
        val partner = getPartnerById(id)
            .fold({ return@transaction it.left() }, { it })

        when (isDebug) {
            true -> PartnerRepository.deletePartner(id)
            else -> PartnerRepository.deletePartner(id)
                .flatMap { KeycloakGroupIntegration.deleteGroup(partner.name) }
                .bimap({ rollback(); it }, { Unit })
        }
    }


    override fun updatePartner(partnerForm: PartnerUpdateForm): Either<ServiceError, Partner> = transaction {
        when (val partner = getPartnerById(partnerForm.id)) {
            is Either.Left -> partner.a.left()
            is Either.Right -> {
                PartnerRepository.updatePartner(partnerForm)
                    .flatMap {
                        partnerForm.name?.let {
                            when (isDebug) {
                                true -> partner.b.right()
                                else -> {
                                    KeycloakGroupIntegration.updateGroup(partner.b.name, partnerForm.name!!)
                                        .fold({ it.left() }, { partner.b.right() })
                                }
                            }
                        } ?: partner.b.right()
                    }
                    .bimap({ rollback(); it }, { it })
            }
        }
    }
}