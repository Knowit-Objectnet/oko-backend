package ombruk.backend.partner.service

import arrow.core.*
import com.typesafe.config.ConfigFactory
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.ClientRequestException
import io.ktor.client.request.*
import io.ktor.config.HoconApplicationConfig
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import ombruk.backend.partner.form.KeycloakPartnerForm
import ombruk.backend.partner.model.TokenResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * This class is used for integration with Keycloak, the provider of OKO's identity management system. In order to
 * properly authorize the users of the application, there was a need for associating specific users to the group they
 * belong to. This is done by adding them to a keycloak group, which has an associated ID that can be used to reject
 * unauthorized API calls so that data that does not belong to an user cannot be altered. Although users have to be added
 * to groups manually, this allows us to automatically create, update and delete the aforementioned groups whenever a
 * partner is changed in the database.
 */
@KtorExperimentalAPI
object KeycloakGroupIntegration {

    private val logger: Logger = LoggerFactory.getLogger("ombruk.partner.service.KeycloakGroupIntegration")
    private val client: HttpClient = HttpClient(Apache)
    private val keycloakBaseUrl: String
    private val tokenUrl: String
    private val groupsUrl: String
    private const val grantType = "client_credentials"
    private const val clientID = "partner-microservice"
    private val clientSecret: String

    private val json = Json(JsonConfiguration.Stable)

    init {
        logger.debug("Trying to initialize KeycloakGroupIntegration")
        val appConfig = HoconApplicationConfig(ConfigFactory.load())
        keycloakBaseUrl = appConfig.property("ktor.keycloak.keycloakUrl").getString()
        clientSecret = appConfig.property("ktor.keycloak.clientSecret").getString()
        tokenUrl = keycloakBaseUrl + "realms/staging/protocol/openid-connect/token"
        groupsUrl = keycloakBaseUrl + "admin/realms/staging/groups/"
        runBlocking { authenticate() }
    }


    /**
     * Helper function used for performing requests towards Keycloak. The function that is passed in will at most
     * be called four times. A failed request will be retried three times before it fails. If the request is rejected
     * due to being unauthenticated, it will automatically re-authenticate. If specified errors occur, it will return instantly.
     *
     * @param function A suspended function that will be performed.
     * @return An [Either] object consisting of a [KeycloakIntegrationError] on failure or the result of the passed in function
     * ([T]) on success.
     *
     */
    private suspend fun <T> performRequest(function: suspend () -> T): Either<KeycloakIntegrationError, T> {
        authenticate()
        var result: Either<KeycloakIntegrationError, T>? = null
        kotlin.runCatching { function() }
            .onFailure {
                if(it is ClientRequestException){
                    logger.warn(it.response.status.value.toString())
                    result =  when (it.response.status.value) {
                        HttpStatusCode.Conflict.value -> Left(KeycloakIntegrationError.ConflictError())
                        else -> Left(KeycloakIntegrationError.KeycloakError("Failed to connect to Keycloak"))
                    }
                }
            }.onSuccess { result = it.right() }
        return result ?: KeycloakIntegrationError.KeycloakError("Failed to perform keycloak request")
            .left()
    }

    private lateinit var token: TokenResponse

    /**
     * Function used to authenticate the service so that groups can be fetched from keycloak. Refresh tokens
     * are not currently being used. Whenever an access token fails, this function is called again. Successful calls will
     * result in the [token] variable being set.
     *
     * @return [Unit]
     */
    private suspend fun authenticate() {
        runCatching {
            client.post<String>(
                tokenUrl
            ) {
                accept(ContentType.Application.Json)
                header("content-type", "application/x-www-form-urlencoded")
                body = "grant_type=$grantType&client_id=$clientID&client_secret=$clientSecret"
            }

        }
            .onFailure { logger.warn("Failed to perform auth request") }
            .fold(
                { token = json.parse(TokenResponse.serializer(), it) },
                { KeycloakIntegrationError.AuthenticationError("Failed to perform auth request") })
    }

    /**
     * Fetches all groups from Keycloak.
     *
     * @return An [Either] object consisting of a [KeycloakIntegrationError] on failure and a [List] of
     * [KeycloakPartnerForm] objects on success.
     */
    private fun getGroups() =
        runBlocking {
            performRequest {
                client.get<String>(
                    groupsUrl
                ) {
                    header(
                        "Authorization",
                        "Bearer " + token.accessToken
                    )
                    accept(ContentType.Application.Json)
                }
            }
        }.bimap({ it }, { json.parse(
            KeycloakPartnerForm.serializer().list, it) })

    /**
     * Helper function for getting a group based on its name. Keycloak only supports fetching groups by their ID, so a solution
     * like this must be used.
     *
     * @param name The name of the group that should be fetched. Returns an error if the group does not exist.
     * @return An [Either] object consisting of either a [KeycloakIntegrationError] or a [KeycloakPartnerForm] on success.
     */
    private fun getGroupByName(name: String): Either<KeycloakIntegrationError, KeycloakPartnerForm> =
        getGroups().map { groups -> groups.firstOrNull { it.name == name } }
            .leftIfNull { KeycloakIntegrationError.NotFoundError("Could not find group with name $name") }

    /**
     * Updates a keycloak group. Currently, only group names can be updated.
     *
     * @param oldName The current name of the keycloak group.
     * @param newName The new name of the keycloak group.
     */
    fun updateGroup(oldName: String, newName: String): Either<KeycloakIntegrationError, Unit> =
        getGroupByName(oldName)
            .map {
                runBlocking {
                    performRequest {
                        client.put<String>(
                            groupsUrl + it.id
                        ) {
                            header(
                                "Authorization",
                                "Bearer " + token.accessToken
                            )
                            accept(ContentType.Application.Json)
                            contentType(ContentType.Application.Json)
                            body = "{\"name\": \"$newName\"}"
                        }
                    }
                }
            }
            .fold({ Left(it) }, { Right(Unit) })

    /**
     * Deletes a group from keycloak. Returns an error if the provided group name does not exist.
     *
     * @param name The name of the group that should be deleted. The exact name passed in must exist in keycloak.
     * @return An [Either] object consisting of either a [KeycloakIntegrationError] on failure or [Unit] on success.
     */
    fun deleteGroup(name: String): Either<KeycloakIntegrationError, Unit> =
        getGroupByName(name)
            .map {
                runBlocking {
                    performRequest {
                        client.delete<String>(
                            groupsUrl + it.id
                        ) {
                            header(
                                "Authorization",
                                "Bearer " + token.accessToken
                            )
                            accept(ContentType.Application.Json)
                        }
                    }
                }
            }
            .fold({ Left(it) }, { Right(Unit) })

    /**
     * Creates a Keycloak group for the specified Partner. This function should be called whenever a new partner is
     * stored to the database. The [id] that is passed in as a parameter will be stored as an attribute in Keycloak, and
     * will be passed along in all tokens provided by keycloak so that individual users can be associated to a larger group,
     * for instance a partner or a reuse station.
     *
     * @param name The name of the group, e.g "Fretex". Must be a name that does not exist in keycloak.
     * @param id The ID of the created group. Must match the ID the partner has in the database.
     * @return An [Either] object, consisting of a [KeycloakIntegrationError] on failure and a [String] on success.
     */
    fun createGroup(name: String, id: Int) = runBlocking {
        logger.debug("Trying to create keycloak group")
        performRequest {
            client.post<String>(
                groupsUrl
            ) {
                header(
                    "Authorization",
                    "Bearer " + token.accessToken
                )
                accept(ContentType.Application.Json)
                contentType(ContentType.Application.Json)
                body = "{\"name\": \"$name\", \"attributes\": {\"GroupID\": [$id]}}"
            }
        }
    }
}