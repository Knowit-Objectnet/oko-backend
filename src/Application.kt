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
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Locations
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.serialization.DefaultJsonConfiguration
import io.ktor.serialization.json
import io.ktor.util.DataConversionException
import io.ktor.util.KtorExperimentalAPI
import kotlinx.serialization.json.Json
import ombruk.backend.calendar.api.events
import ombruk.backend.calendar.api.stations
import ombruk.backend.calendar.service.EventService
import ombruk.backend.calendar.service.StationService
import ombruk.backend.partner.api.partners
import ombruk.backend.partner.service.PartnerService
import ombruk.backend.pickup.api.pickup
import ombruk.backend.pickup.api.request
import ombruk.backend.pickup.service.PickupService
import ombruk.backend.pickup.service.RequestService
import ombruk.backend.reporting.api.report
import ombruk.backend.reporting.service.ReportService
import ombruk.backend.shared.api.Authorization
import ombruk.backend.shared.api.JwtMockConfig
import ombruk.backend.shared.database.initDB
import org.valiktor.ConstraintViolationException
import org.valiktor.i18n.mapToMessage
import java.net.URL
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)


var appConfig = HoconApplicationConfig(ConfigFactory.load())
var debug: Boolean = appConfig.property("ktor.oko.debug").getString().toBoolean()
var keycloakUrl = appConfig.property("ktor.keycloak.keycloakUrl").getString()
var keycloakRealm = appConfig.property("ktor.keycloak.keycloakRealm").getString()

@KtorExperimentalLocationsAPI
@KtorExperimentalAPI
@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    initDB()

    install(Locations)

    install(DataConversion) {
        convert<LocalDateTime> {

            decode { values, _ ->
                values.singleOrNull()?.let { LocalDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME) }
            }

            encode { value ->
                when (value) {
                    null -> listOf()
                    is LocalDateTime -> listOf(value.format(DateTimeFormatter.ISO_DATE_TIME))
                    else -> throw DataConversionException("Cannot convert $value as LocalDateTime")
                }
            }
        }

        convert<LocalTime> {

            decode { values, _ ->
                values.singleOrNull()?.let { LocalTime.parse(it, DateTimeFormatter.ISO_TIME) }
            }

            encode { value ->
                when (value) {
                    null -> listOf()
                    is LocalTime -> listOf(value.format(DateTimeFormatter.ISO_TIME))
                    else -> throw DataConversionException("Cannot convert $value as LocalTime")
                }
            }
        }
    }


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
        header(HttpHeaders.Origin)
        header(HttpHeaders.AccessControlAllowCredentials)
        host(
            host = "oko.knowit.no",
            schemes = listOf("http", "https"),
            subDomains = listOf("staging")
        )
        host(
            host = "0.0.0.0:8080",
            schemes = listOf("http", "https")
        )
        host(
            host = "localhost:8080",
            schemes = listOf("http", "https")
        )
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
        events(EventService)
        partners(PartnerService)
        report(ReportService)
        pickup(PickupService)
        stations(StationService)
        request(RequestService)
        get("/health_check") {
            call.respond(HttpStatusCode.OK)
        }

        install(StatusPages) {
            exception<AuthenticationException> { call.respond(HttpStatusCode.Unauthorized) }
            exception<AuthorizationException> { call.respond(HttpStatusCode.Forbidden) }
            exception<ParameterConversionException> {
                call.respond(
                    HttpStatusCode.BadRequest,
                    "${it.parameterName} could not be parsed."
                )
            }
            exception<ConstraintViolationException> { exception ->
                call.respond(
                    HttpStatusCode.UnprocessableEntity,
                    exception.constraintViolations.mapToMessage().joinToString { "${it.property}: ${it.message}" })
            }
        }

    }
}

class AuthenticationException : RuntimeException()
class AuthorizationException : RuntimeException()