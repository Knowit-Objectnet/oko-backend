package ombruk.backend.aktor.application.service

import arrow.core.Either
import arrow.core.extensions.either.monadError.ensure
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import io.ktor.locations.*
import io.ktor.util.*
import ombruk.backend.aktor.application.api.dto.*
import ombruk.backend.aktor.domain.entity.Verifisering
import ombruk.backend.aktor.domain.entity.VerifiseringStatus
import ombruk.backend.aktor.domain.port.IVerifiseringRepository
import ombruk.backend.shared.error.ServiceError
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class VerifiseringService constructor(
    private val verifiseringRepository: IVerifiseringRepository
) : IVerifiseringService {

    @KtorExperimentalAPI
    override fun save(dto: VerifiseringSaveDto): Either<ServiceError, Verifisering> {
        return transaction {
            verifiseringRepository.insert(dto)
        }
    }

    override fun getVerifiseringById(id: UUID): Either<ServiceError, Verifisering> {
        return transaction { verifiseringRepository.findOne(id) }
    }

    override fun getVerifiseringStatusById(id: UUID): Either<ServiceError, VerifiseringStatus> {
        return getVerifiseringById(id).map { VerifiseringStatus(it.id, it.telefonVerifisert, it.epostVerifisert) }
    }

    @KtorExperimentalLocationsAPI
    override fun verifiser(dto: KontaktVerifiseringDto): Either<ServiceError, VerifiseringStatus> {
        return transaction {
            verifiseringRepository.findOne(dto.id)
                .flatMap { verifisering ->

                    var verifiseringUpdate = VerifiseringUpdateDto(verifisering.id)

                    if ((!verifisering.telefonKode.isNullOrEmpty()) && verifisering.telefonKode == dto.telefonKode) {
                        verifiseringUpdate = verifiseringUpdate.copy(telefonVerifisert = true)
                    }
                    if ((!verifisering.epostKode.isNullOrEmpty()) && verifisering.epostKode == dto.epostKode) {
                        verifiseringUpdate = verifiseringUpdate.copy(epostVerifisert = true)
                    }

                    update(
                        verifiseringUpdate
                    )
                    .ensure(
                        { ServiceError("Verifisering feilet") },
                        {
                            !dto.telefonKode.isNullOrEmpty() && it.telefonVerifisert ||
                                    !dto.epostKode.isNullOrEmpty() && it.epostVerifisert
                        }
                        )
                    .fold(
                        { rollback(); it.left() },
                        {
                            VerifiseringStatus(
                                id = it.id,
                                telefonVerifisert = it.telefonVerifisert,
                                epostVerifisert = it.epostVerifisert
                            ).right()
                        }
                    )
                }
        }
    }

    @KtorExperimentalAPI
    override fun deleteVerifiseringById(id: UUID): Either<ServiceError, Verifisering> {
        return transaction {
            verifiseringRepository.findOne(id).flatMap { verifisering ->
                verifiseringRepository.delete(id)
                    .bimap({ rollback(); it }, { verifisering })
            }
        }
    }

    override fun update(dto: VerifiseringUpdateDto): Either<ServiceError, Verifisering>  {
        return transaction {
            verifiseringRepository.update(
                dto.copy()
            )
        }
    }
}