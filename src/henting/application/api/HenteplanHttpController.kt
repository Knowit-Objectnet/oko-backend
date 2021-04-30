package ombruk.backend.henting.application.api

import io.ktor.routing.*
import ombruk.backend.henting.application.service.IHenteplanService

fun Routing.henteplaner(henteplanService: IHenteplanService) {

    route("henteplaner") {

    }
}