package ombruk.backend.vektregistrering.application.service

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ombruk.backend.kategori.application.api.dto.HenteplanKategoriFindDto
import ombruk.backend.kategori.application.api.dto.KategoriDeleteDto
import ombruk.backend.kategori.application.api.dto.KategoriFindDto
import ombruk.backend.kategori.application.api.dto.KategoriSaveDto
import ombruk.backend.kategori.domain.entity.Kategori
import ombruk.backend.kategori.domain.port.IKategoriRepository
import ombruk.backend.shared.error.ServiceError
import ombruk.backend.vektregistrering.application.api.dto.VektregistreringDeleteDto
import ombruk.backend.vektregistrering.application.api.dto.VektregistreringFindDto
import ombruk.backend.vektregistrering.application.api.dto.VektregistreringSaveDto
import ombruk.backend.vektregistrering.domain.entity.Vektregistrering
import ombruk.backend.vektregistrering.domain.port.IVektregistreringRepository
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class VektregistreringService(
    val vektregistreringRepository: IVektregistreringRepository
    ) : IVektregistreringService {
    override fun save(dto: VektregistreringSaveDto): Either<ServiceError, Vektregistrering> {
        return transaction {
            vektregistreringRepository.insert(dto)
                .fold({ rollback(); it.left() }, { it.right() })
        }
    }

    override fun findOne(id: UUID): Either<ServiceError, Vektregistrering> {
        return transaction {
            vektregistreringRepository.findOne(id)
        }
    }
    override fun find(dto: VektregistreringFindDto): Either<ServiceError, List<Vektregistrering>> {

        return transaction {

            val vektregistreringer = vektregistreringRepository.find(dto)
            vektregistreringer.fold(
                { Either.Left(ServiceError(it.message)) },
                { it.right() }
            )

        }
    }

    override fun delete(id: UUID): Either<ServiceError, Unit> {
        return transaction {
            vektregistreringRepository.delete(id)
                .fold({ rollback(); it.left() }, { it.right() })
        }
    }
}