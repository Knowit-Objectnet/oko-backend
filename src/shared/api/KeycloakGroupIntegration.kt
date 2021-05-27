package ombruk.backend.shared.api

import arrow.core.*
import com.typesafe.config.ConfigFactory
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.ClientRequestException
import io.ktor.client.request.*
import io.ktor.config.HoconApplicationConfig
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import ombruk.backend.partner.model.KeycloakPartner
import ombruk.backend.partner.model.TokenResponse
import ombruk.backend.shared.error.KeycloakIntegrationError
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

/**
 * This class is used for integration with Keycloak, the provider of OKO's identity management system. In order to
 * properly authorize the users of the application, there was a need for associating specific users to the group they
 * belong to. This is done by adding them to a keycloak group, which has an associated ID that can be used to reject
 * unauthorized API calls so that data that does not belong to an user cannot be altered. Although users have to be added
 * to groups manually, this allows us to automatically create, update and delete the aforementioned groups whenever a
 * partner is changed in the database.
 */
@KtorExperimentalAPI
class KeycloakGroupIntegration {

    @KtorExperimentalAPI
    private val appConfig = HoconApplicationConfig(ConfigFactory.load())

    @KtorExperimentalAPI
    private val isDebug: Boolean = appConfig.property("ktor.oko.debug").getString().toBoolean()

    private val logger: Logger = LoggerFactory.getLogger("ombruk.partner.service.KeycloakGroupIntegration")
    private val client: HttpClient = HttpClient(Apache)
    private val keycloakBaseUrl = appConfig.property("ktor.keycloak.keycloakUrl").getString()
    private val keycloakRealm = appConfig.property("ktor.keycloak.keycloakRealm").getString()
    private val tokenUrl = keycloakBaseUrl + "realms/$keycloakRealm/protocol/openid-connect/token"
    private val groupsUrl = keycloakBaseUrl + "admin/realms/$keycloakRealm/groups/"
    private val grantType = "client_credentials"
    private val clientID = "partner-microservice"
    private val clientSecret = appConfig.property("ktor.keycloak.clientSecret").getString()

//    private val json = Json(JsonConfiguration.Stable)

    private lateinit var token: TokenResponse


    init {
        if (!isDebug) runBlocking { authenticate() }  // initialize token once before any calls are made. Not needed for debug.
    }


    /**
     * Helper function for a the [performRequest] helper function. Builds a [HttpRequest] and attempts to send it and
     * receive the response. Also handles any errors that might occur. Parameters should be the same ones passed to
     * [performRequest].
     *
     * @return a [HttpRequestBuilder] to be used in [sendRequest].
     */
    private fun buildRequest(
        method: HttpMethod,
        url: String,
        contentType: ContentType? = null,
        body: String? = null
    ): HttpRequestBuilder {
        val requestBuilder = HttpRequestBuilder()
        requestBuilder.method = method
        contentType?.let { requestBuilder.contentType(it) }
        body?.let { requestBuilder.body = it }
        requestBuilder.accept(ContentType.Application.Json)
        requestBuilder.header("Authorization", "Bearer ${token.accessToken}")
        requestBuilder.url(url)
        return requestBuilder
    }

    /**
     * Sends a request that was built using [buildRequest]
     *
     * @param requestBuilder A [HttpRequestBuilder] that was generated by [buildRequest].
     * @return a [KeycloakIntegrationError] on failure and a Json [String] on success.
     */
    private fun sendRequest(requestBuilder: HttpRequestBuilder) =
        runCatching { runBlocking { client.request(requestBuilder) as String } }
            .onFailure { logger.error(it.message) }
            .fold({ it.right() }, { handleRequestFailure(it) })


    /**
     * Helper function used for performing requests towards the groups endpoint in Keycloak. The function will attempt
     * to re-authenticate before each call.
     *
     * @param method The Http method to use, for instance [HttpMethod.Get]
     * @param url A [String] that represents a keycloak endpoint. Usually a combination of [groupsUrl] and possibly an ID
     * @param contentType A optional [ContentType] that should be used when request bodies are used.
     * @param body A optional request body to be used when posting or patching
     *
     * @return A [KeycloakIntegrationError] on failure and a Json [String] on success.
     */
    private fun performRequest(
        method: HttpMethod,
        url: String,
        contentType: ContentType? = null,
        body: String? = null
    ) = runBlocking { authenticate() }
        .map { buildRequest(method, url, contentType, body) }
        .flatMap { sendRequest(it) }
        .fold({ it.left() }, { it.right() })


    /**
     * Handles errors that occurs when a request is sent and received. Helper function for [sendRequest].
     * @param e A [Throwable] that occurs during a request.
     * @return A [KeycloakIntegrationError] that better represents what went wrong during the request.
     */
    private fun handleRequestFailure(e: Throwable) = when (e) {
        is ClientRequestException -> when (e.response.status.value) {
            HttpStatusCode.Conflict.value -> KeycloakIntegrationError.ConflictError()
                .left()
            else -> KeycloakIntegrationError.KeycloakError("Failed to connect to Keycloak")
                .left()
        }
        else -> {
            logger.error("HELLO")
            print(e.stackTrace)
            logger.error(e.message)
            KeycloakIntegrationError.KeycloakError("Failed to perform Keycloak request").left()
        }
    }


    /**
     * Function used to authenticate the service so that groups can be fetched from keycloak. Refresh tokens
     * are not currently being used. Whenever an access token fails, this function is called again. Successful calls will
     * result in the [token] variable being set.
     *
     * @return A [KeycloakIntegrationError.AuthenticationError] on failure and [Unit] on success.
     */
    private suspend fun authenticate() = runCatching {
        client.post<String>(tokenUrl) {
            accept(ContentType.Application.Json)
            header("content-type", "application/x-www-form-urlencoded")
            body = "grant_type=$grantType&client_id=$clientID&client_secret=$clientSecret"
        }
    }
        .onFailure {
            logger.warn("Failed to perform auth request")
            print(it.message)
        }
        .fold(
            { token = Json.decodeFromString<TokenResponse>(TokenResponse.serializer(), it); Unit.right() },
            {
                KeycloakIntegrationError.AuthenticationError("Failed to perform auth request")
                    .left()
            }
        )


    /**
     * Fetches all groups from Keycloak.
     *
     * @return An [Either] object consisting of a [KeycloakIntegrationError] on failure and a [List] of
     * [KeycloakPartner] objects on success.
     */
    private fun getGroups(): Either<KeycloakIntegrationError, List<KeycloakPartner>> =
        performRequest(method = HttpMethod.Get, url = groupsUrl)
            .fold(
                { it.left() },
                { Json.decodeFromString(ListSerializer(KeycloakPartner.serializer()), it).right() }
            )

    /**
     * Helper function for getting a group based on its name. Keycloak only supports fetching groups by their ID, so a solution
     * like this must be used.
     *
     * @param name The name of the group that should be fetched. Returns an error if the group does not exist.
     * @return An [Either] object consisting of either a [KeycloakIntegrationError] or a [KeycloakPartner] on success.
     */
    private fun getGroupByName(name: String) = getGroups()
        .map { groups -> groups.firstOrNull { it.name == name } }
        .leftIfNull { KeycloakIntegrationError.NotFoundError("Could not find group with name $name") }


    /**
     * Updates a keycloak group. Currently, only group names can be updated.
     *
     * @param oldName The current name of the keycloak group.
     * @param newName The new name of the keycloak group.
     *
     * @return a [KeycloakIntegrationError] on failure and a [Unit] on success.
     */
    fun updateGroup(oldName: String, newName: String?) = takeIf { isDebug || newName == null }?.let { Unit.right() }
        ?: getGroupByName(oldName)
            .flatMap {
                performRequest(
                    method = HttpMethod.Put,
                    url = groupsUrl + it.id,
                    contentType = ContentType.Application.Json,
                    body = "{\"name\": \"$newName\"}"
                )
                    .fold({ it.left() }, { Unit.right() })
            }

    /**
     * Deletes a group from keycloak. Returns an error if the provided group name does not exist.
     *
     * @param name The name of the group that should be deleted. The exact name passed in must exist in keycloak.
     * @return An [Either] object consisting of either a [KeycloakIntegrationError] on failure or [Unit] on success.
     */
    fun deleteGroup(name: String) = takeIf { isDebug }?.let { Unit.right() }
        ?: getGroupByName(name)
            .flatMap {
                performRequest(
                    method = HttpMethod.Delete,
                    url = groupsUrl + it.id
                )
            }


    /**
     * Creates a Keycloak group for the specified resource. This function should be called whenever a new partner is
     * stored to the database. The [id] that is passed in as a parameter will be stored as an attribute in Keycloak, and
     * will be passed along in all tokens provided by keycloak so that individual users can be associated to a larger group,
     * for instance a partner or a reuse station.
     *
     * @param name The name of the group, e.g "Fretex". Must be a name that does not exist in keycloak.
     * @param id The ID of the created group. Must match the ID the partner has in the database.
     * @return An [Either] object, consisting of a [KeycloakIntegrationError] on failure and a [String] on success.
     */
    fun createGroup(name: String, id: String) = takeIf { isDebug }?.let { Unit.right() }
        ?: runBlocking {
            performRequest(
                HttpMethod.Post,
                url = groupsUrl,
                contentType = ContentType.Application.Json,
                body = "{\"name\": \"$name\", \"attributes\": {\"GroupID\": [$id]}}"
            )
        }

    fun createGroup(name: String, id: Int): Either<KeycloakIntegrationError, Any> {
        return createGroup(name, id.toString())
    }

    fun createGroup(name: String, id: UUID): Either<KeycloakIntegrationError, Any> {
        return createGroup(name, id.toString())
    }
}