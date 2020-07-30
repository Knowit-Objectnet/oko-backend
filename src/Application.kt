package ombruk.backend

import com.auth0.jwk.JwkProviderBuilder
import com.typesafe.config.ConfigFactory
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.auth.jwt.jwt
import io.ktor.config.HoconApplicationConfig
import io.ktor.features.*
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.serialization.DefaultJsonConfiguration
import io.ktor.serialization.json
import kotlinx.serialization.json.Json
import ombruk.backend.calendar.api.events
import ombruk.backend.calendar.api.stations
import ombruk.backend.shared.database.initDB
import ombruk.backend.calendar.service.EventService
import ombruk.backend.calendar.service.StationService
import ombruk.backend.partner.api.partners
import ombruk.backend.partner.service.PartnerService
import ombruk.backend.pickup.api.pickup
import ombruk.backend.reporting.api.report
import ombruk.backend.pickup.service.PickupService
import ombruk.backend.reporting.service.ReportService
import ombruk.backend.shared.api.Authorization
import ombruk.backend.shared.api.JwtMockConfig
import java.net.URL
import java.util.concurrent.TimeUnit

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

var appConfig = HoconApplicationConfig(ConfigFactory.load())
var debug: Boolean = appConfig.property("ktor.oko.debug").getString().toBoolean()
var keycloakUrl = appConfig.property("ktor.keycloak.keycloakUrl").getString()
var keycloakRealm = appConfig.property("ktor.keycloak.keycloakRealm").getString()

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    initDB()

    install(Authentication) {
        if (testing || debug) {    //create a mock service for authorization
            jwt {
                if (testing) Authorization.testing = true
                verifier(JwtMockConfig.createMockVerifier())
                realm = "local testing"
                validate { jwt ->
                    if (jwt.payload.audience.contains("account")) JWTPrincipal(jwt.payload)
                    else null
                }
            }
        } else {
            val jwkIssuer = URL("${keycloakUrl}realms/${keycloakRealm}/protocol/openid-connect/certs")
            val jwkRealm = "OKO backend"
            val jwkProvider = JwkProviderBuilder(jwkIssuer)
                .cached(10, 24, TimeUnit.HOURS)
                .rateLimited(10, 1, TimeUnit.HOURS)
                .build()

            jwt {
                verifier(jwkProvider)
                realm = jwkRealm
                validate { jwt ->
                    if (jwt.payload.audience.contains("account")) JWTPrincipal(jwt.payload)
                    else null
                }
            }
        }
    }

    install(Compression) {
        gzip { priority = 1.0 }
        deflate {
            priority = 10.0
            minimumSize(1024) // condition
        }
    }

    install(DefaultHeaders)
    install(ConditionalHeaders)

    install(CORS) {
        method(HttpMethod.Options)
        method(HttpMethod.Put)
        method(HttpMethod.Delete)
        method(HttpMethod.Patch)
        header(HttpHeaders.Authorization)
        header(HttpHeaders.AccessControlAllowCredentials)
        //anyHost() // @TODO: Don't do this in production if possible. Try to limit it.
        host("0.0.0.0:8080")
        host("staging.oko.knowit.no")
        allowCredentials = true
        allowNonSimpleContentTypes = true
    }

    install(ContentNegotiation) {
        this.json(
            json = Json(DefaultJsonConfiguration.copy(prettyPrint = true)),
            contentType = ContentType.Application.Json
        )
    }

    routing {
        events(EventService(ReportService))
        partners(PartnerService())
        report(ReportService)
        pickup(PickupService)
        stations(StationService)
        get("/health_check") {
            call.respond(HttpStatusCode.OK)
        }

        install(StatusPages) {
            exception<AuthenticationException> { call.respond(HttpStatusCode.Unauthorized) }
            exception<AuthorizationException> { call.respond(HttpStatusCode.Forbidden) }
        }

    }
}

class AuthenticationException : RuntimeException()
class AuthorizationException : RuntimeException()
