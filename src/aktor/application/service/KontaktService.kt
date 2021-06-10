package ombruk.backend.aktor.application.service

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.right
import io.ktor.locations.*
import io.ktor.util.*
import ombruk.backend.aktor.application.api.dto.*
import ombruk.backend.aktor.domain.entity.Kontakt
import ombruk.backend.aktor.domain.port.IKontaktRepository
import ombruk.backend.shared.api.KeycloakGroupIntegration
import ombruk.backend.shared.error.ServiceError
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class KontaktService constructor(
    private val kontaktRepository: IKontaktRepository
) :
    IKontaktService {

    @KtorExperimentalAPI
    override fun save(dto: KontaktSaveDto): Either<ServiceError, Kontakt> {
        return transaction {
            kontaktRepository.insert(dto).flatMap { kontakt ->
                kontakt.right()
                    .bimap({ rollback(); it }, { kontakt })
            }
        }
    }

    override fun getKontaktById(id: UUID): Either<ServiceError, Kontakt> {
        return transaction { kontaktRepository.findOne(id) }
    }

    @KtorExperimentalLocationsAPI
    override fun getKontakter(dto: KontaktGetDto): Either<ServiceError, List<Kontakt>> {
        return transaction { kontaktRepository.find(dto) }
    }

    @KtorExperimentalAPI
    override fun deleteKontaktById(id: UUID): Either<ServiceError, Kontakt> {
        return transaction {
            kontaktRepository.findOne(id).flatMap { kontakt ->
                kontaktRepository.delete(id)
                    .bimap({ rollback(); it }, { kontakt })
            }
        }
    }

    @KtorExperimentalAPI
    override fun update(dto: KontaktUpdateDto): Either<ServiceError, Kontakt> = transaction {
        getKontaktById(dto.id).flatMap { kontakt ->
            kontaktRepository.update(dto).flatMap { newKontakt ->
                newKontakt.right()
                    .bimap({ rollback(); it }, { newKontakt })
            }
        }
    }
}