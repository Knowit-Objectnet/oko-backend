package no.oslokommune.ombruk

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
import io.ktor.http.content.files
import io.ktor.http.content.static
import io.ktor.http.content.staticRootFolder
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Locations
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.serialization.DefaultJsonConfiguration
import io.ktor.serialization.json
import io.ktor.util.DataConversionException
import io.ktor.util.KtorExperimentalAPI
import io.swagger.v3.core.converter.ModelConverters
import io.swagger.v3.core.filter.OpenAPISpecFilter
import io.swagger.v3.core.filter.SpecFilter
import io.swagger.v3.core.util.Yaml
import io.swagger.v3.jaxrs2.Reader
import io.swagger.v3.jaxrs2.ext.OpenAPIExtension
import io.swagger.v3.jaxrs2.ext.OpenAPIExtensions
import io.swagger.v3.oas.integration.SwaggerConfiguration
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import kotlinx.serialization.json.Json
import no.oslokommune.ombruk.partner.api.partnere
import no.oslokommune.ombruk.partner.model.Partner
import no.oslokommune.ombruk.partner.service.IPartnerService
import no.oslokommune.ombruk.partner.service.PartnerService
import no.oslokommune.ombruk.shared.api.Authorization
import no.oslokommune.ombruk.shared.api.JwtMockConfig
import no.oslokommune.ombruk.shared.database.initDB
import no.oslokommune.ombruk.shared.swagger.EitherFilter
import no.oslokommune.ombruk.shared.swagger.LocalTimeConverter
import no.oslokommune.ombruk.shared.swagger.Modifier
import no.oslokommune.ombruk.shared.swagger.extensions.DefaultResponseExtension
import no.oslokommune.ombruk.shared.swagger.extensions.ParameterFileExtraction
import no.oslokommune.ombruk.stasjon.api.stasjoner
import no.oslokommune.ombruk.stasjon.service.IStasjonService
import no.oslokommune.ombruk.stasjon.service.StasjonService
import no.oslokommune.ombruk.uttak.api.uttak
import no.oslokommune.ombruk.uttak.service.IUttakService
import no.oslokommune.ombruk.uttak.service.UttakService
import no.oslokommune.ombruk.uttaksdata.api.uttaksdata
import no.oslokommune.ombruk.uttaksdata.service.IUttaksDataService
import no.oslokommune.ombruk.uttaksdata.service.UttaksDataService
import no.oslokommune.ombruk.uttaksforesporsel.api.request
import no.oslokommune.ombruk.uttaksforesporsel.service.UttaksforesporselService
import org.valiktor.ConstraintViolationException
import org.valiktor.i18n.mapToMessage
import java.io.File
import java.net.URL
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.reflect.jvm.jvmName

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

    //Seems like you can only load one extension at a time..

    val config = SwaggerConfiguration()
    config.openAPI = OpenAPI()
    println(config.openAPI)
    config.openAPI.components = Components()
    val reader = Reader(config)
    ModelConverters.getInstance().addConverter(LocalTimeConverter())
    OpenAPIExtensions.getExtensions().add(ParameterFileExtraction(reader.openAPI))
    val openAPI: OpenAPI = reader.read(
        setOf(
            IPartnerService::class.java,
            IStasjonService::class.java,
            Modifier::class.java,
            IUttakService::class.java,
            IUttaksDataService::class.java
        )
    )

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
            val jwkIssuer = URL("${keycloakUrl}realms/$keycloakRealm/protocol/openid-connect/certs")
            //val jwkRealm = "OKO backend"
            val jwkRealm = "test"
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
            subDomains = listOf("staging", "test", "production")
        )
        host(
            host = "0.0.0.0:8080",
            schemes = listOf("http", "https"),
            subDomains = listOf("staging", "test", "production")
        )
        host(
            host = "localhost:8080",
            schemes = listOf("http", "https"),
            subDomains = listOf("staging", "test", "production")
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
        uttak(UttakService, UttaksDataService)
        partnere(PartnerService)
        uttaksdata(UttaksDataService, UttakService)
        stasjoner(StasjonService)
        request(UttaksforesporselService)
        get("/health_check") {
            call.respond(HttpStatusCode.OK)
        }
        static("swagger-ui") {
            staticRootFolder = File("src/main/resources/static")
            files(File("dist"))
        }
        get("/openapi") {
            call.respondText { Yaml.pretty(openAPI) }
        }
        get("/") {
            call.respondRedirect("/swagger-ui/index.html?url=/openapi")
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

    print(
        """
        
        Debug mode enabled: $debug
        
    """.trimIndent()
    )
}

class AuthenticationException : RuntimeException()
class AuthorizationException : RuntimeException()
