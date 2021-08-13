package ombruk.backend.henting.application.api

import arrow.core.Either
import arrow.core.extensions.either.monad.flatMap
import arrow.core.extensions.either.monadError.ensure
import arrow.core.filterOrElse
import arrow.core.filterOrOther
import arrow.core.flatMap
import henting.application.api.dto.HenteplanSaveDto
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import ombruk.backend.avtale.application.service.AvtaleService
import ombruk.backend.avtale.application.service.IAvtaleService
import ombruk.backend.henting.application.api.dto.*
import ombruk.backend.henting.application.service.IHenteplanService
import ombruk.backend.henting.domain.entity.Henteplan
import ombruk.backend.kategori.application.api.dto.HenteplanKategoriFindDto
import ombruk.backend.kategori.application.api.dto.HenteplanKategoriSaveDto
import ombruk.backend.kategori.application.service.IHenteplanKategoriService
import ombruk.backend.shared.api.Authorization
import ombruk.backend.shared.api.Roles
import ombruk.backend.shared.api.generateResponse
import ombruk.backend.shared.api.receiveCatching
import ombruk.backend.shared.error.AuthorizationError
import ombruk.backend.shared.error.ServiceError
import java.util.*

@KtorExperimentalLocationsAPI
fun Routing.henteplaner(henteplanService: IHenteplanService, avtaleService: IAvtaleService) {

    route("/henteplaner") {

        authenticate {
            get<HenteplanFindOneDto> { form ->
                Authorization.authorizeRole(listOf(Roles.RegEmployee, Roles.Partner, Roles.ReuseStation), call)
                    .flatMap { (role, groupId) ->
                        form.validOrError()
                            .flatMap { henteplanService.findOne(form.id) }
                            .ensure(
                                {AuthorizationError.AccessViolationError("Denne henteplanen tilhører ikke deg.")},
                                { henteplan ->
                                    avtaleService.findOne(henteplan.avtaleId)
                                        .map{
                                            when (role) {
                                                Roles.RegEmployee -> true
                                                Roles.Partner -> groupId == it.aktorId
                                                Roles.ReuseStation -> groupId == henteplan.stasjonId || groupId == it.aktorId
                                            }
                                        }
                                        .fold({false}, {it})
                                }
                            )
                    }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        authenticate {
            get<HenteplanFindByAvtaleIdDto> { form ->
                Authorization.authorizeRole(listOf(Roles.RegEmployee, Roles.Partner, Roles.ReuseStation), call)
                    .flatMap { (role, groupId) ->
                        form.validOrError()
                            .ensure(
                                {AuthorizationError.AccessViolationError("Denne avtalen tilhører ikke deg.")},
                                {
                                    avtaleService.findOne(it.avtaleId)
                                        .map{
                                            when (role) {
                                                Roles.RegEmployee -> true
                                                Roles.Partner -> groupId == it.aktorId
                                                Roles.ReuseStation -> groupId == it.aktorId || it.henteplaner.any { it.stasjonId == groupId }
                                            }
                                        }
                                        .fold({false}, {it})
                                }
                            )
                            .flatMap { henteplanService.findAllForAvtale(form.avtaleId) }
                    }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        authenticate {
            get<HenteplanFindDto> { form ->
                Authorization.authorizeRole(listOf(Roles.RegEmployee, Roles.Partner, Roles.ReuseStation), call)
                    .flatMap { (role, groupId) ->
                        form.validOrError()
                            .flatMap { henteplanService.find(form) }
                            .map { it ->
                                it.filter { henteplan ->
                                avtaleService.findOne(henteplan.avtaleId)
                                    .map{ avtale ->
                                        when (role) {
                                            Roles.RegEmployee -> true
                                            Roles.Partner -> groupId == avtale.aktorId
                                            Roles.ReuseStation -> groupId == henteplan.stasjonId || groupId == avtale.aktorId
                                        }
                                    }
                                    .fold({false}, {it})
                            } }
                    }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        authenticate {
            post {
                Authorization.authorizeRole(listOf(Roles.RegEmployee), call)
                    .flatMap { receiveCatching { call.receive<HenteplanSaveDto>() } }
                    .flatMap { it.validOrError() }
                    .flatMap { henteplanService.save(it) }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        authenticate {
            delete<HenteplanDeleteDto> { form ->
                Authorization.authorizeRole(listOf(Roles.RegEmployee), call)
                    .flatMap { form.validOrError() }
                    .flatMap { henteplanService.archiveOne(form.id) }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }

        authenticate {
            patch {
                Authorization.authorizeRole(listOf(Roles.RegEmployee), call)
                    .flatMap { receiveCatching { call.receive<HenteplanUpdateDto>() } }
                    .flatMap { it.validOrError() }
                    .flatMap { henteplanService.update(it) }
                    .run { generateResponse(this) }
                    .also { (code, response) -> call.respond(code, response) }
            }
        }
    }
}