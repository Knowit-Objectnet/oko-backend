package no.oslokommune.ombruk.shared.api

import com.auth0.jwt.JWT
import com.auth0.jwt.JWT.require
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.auth.jwt.JWTPrincipal
import java.util.*

/**
 * This object exposes a mock verifier and a mock token generator, and should solely be used for testing purposes.
 */
object JwtMockConfig {

    private const val secret = "testing123456789"
    private val algorithm = Algorithm.HMAC256(secret)
    val partnerBearer1 =
        "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJhdWQiOiJhY2NvdW50Iiwic3ViIjoiQXV0aGVudGljYXRpb24iLCJyb2xlcyI6WyJwYXJ0bmVyIl0sImV4cCI6OTIyMzM3MjAzNjg1NDc3NSwiR3JvdXBJRCI6MX0.cHb5E8Jr1qOr6Lxen93y4a6640Arv8dl0mLdzEvtTnE_Oj4tWOI1zxW02Mst0HE066fQhwgfNJVHLYYcS21Yfg"
    val partnerBearer2 =
        "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJhdWQiOiJhY2NvdW50Iiwic3ViIjoiQXV0aGVudGljYXRpb24iLCJyb2xlcyI6WyJwYXJ0bmVyIl0sImV4cCI6OTIyMzM3MjAzNjg1NDc3NSwiR3JvdXBJRCI6Mn0.Mzn2cdMiSqIDUCkHpdiaG-hEM2NGcbLOKlkFeOHIhEHXYcdEDvzfg9lrdO24_Hjdk1XxGtBjZbVm0VVw-3Xgcg"
    val regEmployeeBearer =
        "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJhdWQiOiJhY2NvdW50Iiwic3ViIjoiQXV0aGVudGljYXRpb24iLCJyb2xlcyI6WyJyZWdfZW1wbG95ZWUiXSwiZXhwIjo5MjIzMzcyMDM2ODU0Nzc1LCJHcm91cElEIjoyfQ.vi42IxWDGPOvxgiThFwfpv6Rif9QRzIXy0MBSLOasT_1AQlUWD8NmUWexaJLQ-WpUmkbL1zzqaEF4xPdOkJ2_Q"
    val reuseStasjonBearer =
        "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJhdWQiOiJhY2NvdW50Iiwic3ViIjoiQXV0aGVudGljYXRpb24iLCJyb2xlcyI6WyJyZXVzZV9zdGF0aW9uIl0sImV4cCI6OTIyMzM3MjAzNjg1NDc3NSwiR3JvdXBJRCI6Mn0.tIx5MsiMh2kftFpPnIKAcEiqIr_5RcLxx8QcIUPV7yKtXgtJykh6W1xo4MNtd1Wh97AOQ2tCSfMlAFno8RYyjw"
    private val mockTokens = listOf(
        Pair("Partner with ID 1", partnerBearer1),
        Pair("Partner with ID 2", partnerBearer2),
        Pair("reg_employee", regEmployeeBearer),
        Pair("reuse_stasjon", reuseStasjonBearer)
    )

    /**
     * Creates a mock verifier that accepts local tokens.
     */
    fun createMockVerifier(): JWTVerifier {
        println("\nMOCK CREDENTIALS\n")
        println("ID for both reuse_stasjon and reg_employee is not validated and can be whatever")
        mockTokens.forEach { (info, token) -> println("$info:\n$token") }
        println()
        return require(algorithm)
            .ignoreIssuedAt()
            .build()
    }

    /**
     * Function used for generating mock tokens. Generated tokens expire in April of 2262. Will be deprecated in 2261.
     * @param id The GroupID [Int] you want to mock. This must correspond with a keycloak GroupID.
     * @param roles A [List] of [String] objects. Must correspond with the string values of the roles enum in [Authorization]
     * @return A [String] version of a [JWTPrincipal] that the application will accept as valid.
     */
    fun makeMockToken(id: Int, roles: List<String>): String {
        return JWT.create()
            .withAudience("account")
            .withSubject("Authentication")
            .withClaim("GroupID", id)
            .withArrayClaim("roles", roles.toTypedArray())
            .withExpiresAt(Date(Long.MAX_VALUE))
            .sign(algorithm)
    }
}