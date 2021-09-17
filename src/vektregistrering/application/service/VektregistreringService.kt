package ombruk.backend.vektregistrering.application.service

import arrow.core.*
import arrow.core.extensions.either.applicative.applicative
import arrow.core.extensions.list.traverse.sequence
import ombruk.backend.henting.application.api.dto.PlanlagtHentingUpdateDto
import ombruk.backend.henting.domain.entity.PlanlagtHenting
import ombruk.backend.kategori.application.api.dto.HenteplanKategoriFindDto
import ombruk.backend.kategori.application.api.dto.KategoriDeleteDto
import ombruk.backend.kategori.application.api.dto.KategoriFindDto
import ombruk.backend.kategori.application.api.dto.KategoriSaveDto
import ombruk.backend.kategori.domain.entity.Kategori
import ombruk.backend.kategori.domain.port.IKategoriRepository
import ombruk.backend.shared.error.ServiceError
import ombruk.backend.utlysning.application.api.dto.UtlysningBatchSaveDto
import ombruk.backend.utlysning.application.api.dto.UtlysningFindDto
import ombruk.backend.utlysning.application.api.dto.UtlysningSaveDto
import ombruk.backend.utlysning.domain.entity.Utlysning
import ombruk.backend.vektregistrering.application.api.dto.*
import ombruk.backend.vektregistrering.domain.entity.Vektregistrering
import ombruk.backend.vektregistrering.domain.port.IVektregistreringRepository
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
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

    override fun batchSave(dto: VektregistreringBatchSaveDto): Either<ServiceError, List<Vektregistrering>> {
        return transaction {
            dto.kategoriIds
                .mapIndexed { index, kategoriId -> save(
                    VektregistreringSaveDto(
                    hentingId = dto.hentingId,
                    kategoriId = UUID.fromString(kategoriId),
                    vekt = dto.veiinger.get(index)))
                }
                .sequence(Either.applicative())
                .fix()
                .map { it.fix() }
                .fold({rollback(); it.left()}, {it.right()})
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

    override fun update(dto: VektregistreringUpdateDto): Either<ServiceError, Vektregistrering> {
        return transaction { vektregistreringRepository.update(dto) }
    }

    override fun batchUpdate(dto: VektregistreringBatchUpdateDto): Either<ServiceError, List<Vektregistrering>> {
        return transaction {
            dto.vektregistreringIds
                .mapIndexed { index, veiingId -> update(
                    VektregistreringUpdateDto(
                        id = UUID.fromString(veiingId),
                        hentingId = dto.hentingId,
                        vekt = dto.veiinger.get(index),
                        vektRegistreringAv = dto.vektRegistreringAv)
                )
                }
                .sequence(Either.applicative())
                .fix()
                .map { it.fix() }
                .fold({rollback(); it.left()}, {it.right()})
        }
    }
}