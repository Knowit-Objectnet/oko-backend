package ombruk.backend.aktor.application.service

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import io.ktor.locations.*
import io.ktor.util.*
import ombruk.backend.aktor.application.api.dto.*
import ombruk.backend.aktor.domain.entity.Verifisering
import ombruk.backend.aktor.domain.entity.Verifisert
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
            if(dto.telefonKode == null && dto.epostKode == null) Either.Left(ServiceError("Ingen koder generert"))
            else verifiseringRepository.insert(dto)
        }
    }

    override fun getVerifiseringById(id: UUID): Either<ServiceError, Verifisering> {
        return transaction { verifiseringRepository.findOne(id) }
    }

    @KtorExperimentalLocationsAPI
    override fun verifiser(dto: KontaktVerifiseringDto): Either<ServiceError, Verifisert> {
        return transaction {
            verifiseringRepository.findOne(dto.id)
                .flatMap { verifisering ->



                    var verifiseringUpdate: VerifiseringUpdateDto = VerifiseringUpdateDto(verifisering.id)

                    if (!verifisering.telefonKode.isNullOrEmpty() && verifisering.telefonKode == dto.telefonKode) {
                        verifiseringUpdate = VerifiseringUpdateDto(
                            id = verifisering.id,
                            telefonKode = null,
                            telefonVerifisert = true
                        )
                    } else if (!verifisering.epostKode.isNullOrEmpty() && verifisering.epostKode == dto.epostKode) {
                        verifiseringUpdate = VerifiseringUpdateDto(
                            id = verifisering.id,
                            epostKode = null,
                            epostVerifisert = true
                        )
                    }

                    update(
                        verifiseringUpdate
                    ).fold(
                        { rollback(); it.left() },
                        {
                            Verifisert(
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
        return transaction { verifiseringRepository.update(dto) }
    }
}