package no.oslokommune.ombruk.shared.swagger

import io.swagger.v3.jaxrs2.Reader
import io.swagger.v3.jaxrs2.ReaderListener
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.security.OAuthFlow
import io.swagger.v3.oas.models.security.OAuthFlows
import io.swagger.v3.oas.models.security.Scopes
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import no.oslokommune.ombruk.keycloakRealm
import no.oslokommune.ombruk.keycloakUrl

class Modifier : ReaderListener {
    override fun beforeScan(reader: Reader?, openAPI: OpenAPI?) {
    }

    override fun afterScan(reader: Reader?, openAPI: OpenAPI) {
        openAPI?.components?.schemas?.filter { it.key.contains("Either") }
        openAPI.components.schemas.filter { it.key.startsWith("Either") }.forEach { openAPI.components.schemas.remove(it.key) }
//        val scheme = SecurityScheme()
//        scheme.name = "security"
//        scheme.type = SecurityScheme.Type.OAUTH2
//        scheme.`in` = SecurityScheme.In.HEADER
//        val flows = OAuthFlows()
//        flows.implicit = OAuthFlow()
//        flows.implicit.authorizationUrl = ""
//        flows.implicit.tokenUrl = ""
//        flows.implicit.scopes = Scopes()
//        val scheme = SecurityScheme().type(SecurityScheme.Type.OAUTH2)
//            .`in`(SecurityScheme.In.HEADER)
//            .flows(OAuthFlows().implicit(OAuthFlow()
//                .authorizationUrl("$keycloakUrl/auth/realms/$keycloakRealm/protocol/openid-connect/auth")
//                .tokenUrl("${keycloakUrl}/$keycloakRealm/protocol/openid-connect/token")
//                .scopes(Scopes().addString("openid", null))
//            ))
//        scheme.type = SecurityScheme.Type.OAUTH2
//        scheme.`in` = SecurityScheme.In.HEADER
//        val flows = OAuthFlows()
//        flows.implicit = OAuthFlow()
//            .authorizationUrl("$keycloakUrl/auth/realms/$keycloakRealm/protocol/openid-connect/auth")
//            .tokenUrl("${keycloakUrl}/$keycloakRealm/protocol/openid-connect/token")
//            .scopes(Scopes().addString("openid", null))
        openAPI.components.securitySchemes["security"]!!.flows.authorizationCode.authorizationUrl = "${keycloakUrl}realms/$keycloakRealm/protocol/openid-connect/auth"
        openAPI.components.securitySchemes["security"]!!.flows.authorizationCode.tokenUrl = "${keycloakUrl}/$keycloakRealm/protocol/openid-connect/token"
//        openAPI.components.addSecuritySchemes("security", scheme)
//        openAPI.addServersItem(Server().url("api.knowit.no"))
    }
}