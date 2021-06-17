package ombruk.backend.shared.api

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
    private val algorithm = Algorithm.HMAC512(secret)
    const val regEmployeeBearer =
        "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJhdWQiOiJhY2NvdW50Iiwic3ViIjoiQXV0aGVudGljYXRpb24iLCJyb2xlcyI6WyJyZWdfZW1wbG95ZWUiXSwiZXhwIjo5MjIzMzcyMDM2ODU0Nzc1LCJHcm91cElEIjoyfQ.vi42IxWDGPOvxgiThFwfpv6Rif9QRzIXy0MBSLOasT_1AQlUWD8NmUWexaJLQ-WpUmkbL1zzqaEF4xPdOkJ2_Q"
    const val partnerBearerUUID1 =
        "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJhdWQiOiJhY2NvdW50Iiwic3ViIjoiQXV0aGVudGljYXRpb24iLCJyb2xlcyI6WyJwYXJ0bmVyIl0sImV4cCI6OTIyMzM3MjAzNjg1NDc3NSwiR3JvdXBJRCI6IjJlZjEzYjg1LTRlZjgtNGRlYy04MWM5LWIyY2JjNjdkMWMxMSJ9.AXSHEBeEy3I8p9M-K2I7aic2j7tA8Bte6raStzJejAJMPkMCVQ07XRDw6Am29YQuNYx1fLvnCVcQKfeovwpOwQ"
    const val partnerBearerUUID2 =
        "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJhdWQiOiJhY2NvdW50Iiwic3ViIjoiQXV0aGVudGljYXRpb24iLCJyb2xlcyI6WyJwYXJ0bmVyIl0sImV4cCI6OTIyMzM3MjAzNjg1NDc3NSwiR3JvdXBJRCI6IjU3MWExZDdlLTZmMjctNDFiNy1iODAwLTI2MGY0Yzg0NmVmYiJ9.i0ZqneW2i5k4n-3dwvWBm2jpYJbFE0FiFOjJXe27Xncz01u4HIgiYg0cjLsdiuYF9nWQdqDe3pcdL8EDrJ59fw"
    const val stasjonBearerGronmo =
        "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJhdWQiOiJhY2NvdW50Iiwic3ViIjoiQXV0aGVudGljYXRpb24iLCJyb2xlcyI6WyJyZXVzZV9zdGF0aW9uIl0sImV4cCI6OTIyMzM3MjAzNjg1NDc3NSwiR3JvdXBJRCI6IjgzYzliNTM0LTdiMmEtNDZkNC05ZTAyLTJiZGFiMzg2MDFjMiJ9.tAbE6HVumqjWbqSXXTLhiWQ0WFDespcy-C8FX0oFKfFaZZAHoklY9WHXAEjtO9FVKiwgCfihodzMEMcyZKbrug"
    const val stasjonBearerHaraldrud =
        "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJhdWQiOiJhY2NvdW50Iiwic3ViIjoiQXV0aGVudGljYXRpb24iLCJyb2xlcyI6WyJyZXVzZV9zdGF0aW9uIl0sImV4cCI6OTIyMzM3MjAzNjg1NDc3NSwiR3JvdXBJRCI6ImRkZjU1ZmQ1LWU3MTEtNDNhNC05MzQ1LTQyNzBlNWQxMjVkYyJ9.B3Oq0cSXlACRO6nvYOmgiIiKMRG1rOiv9Zo-Ycq2SFQPD7takPpsKpiQjMJzy5RsWdTRlnpflKqonoyjmJpXYw"

    private val mockTokens = listOf(
        Pair("reg_employee", regEmployeeBearer),
        Pair("Partner UUID1: 2ef13b85-4ef8-4dec-81c9-b2cbc67d1c11", partnerBearerUUID1),
        Pair("Partner UUID2: 571a1d7e-6f27-41b7-b800-260f4c846efb", partnerBearerUUID2),
        Pair("Stasjon Grønmo", stasjonBearerGronmo),
        Pair("Stasjon Haraldrud", stasjonBearerHaraldrud),
    )

    /**
     * Creates a mock verifier that accepts local tokens.
     */
    fun createMockVerifier(): JWTVerifier {
        println("\nMOCK CREDENTIALS\n")
        println("ID for reg_employee is not validated and can be whatever")
        mockTokens.forEach { (info, token) -> println("$info:\n$token") }
        println()
        return require(algorithm)
            .ignoreIssuedAt()
            .build()
    }

    /**
     * Function used for generating mock tokens. Generated tokens expire in April of 2262. Will be deprecated in 2261.
     * @param id The GroupID [UUID] you want to mock. This must correspond with a keycloak GroupID.
     * @param roles A [List] of [String] objects. Must correspond with the string values of the roles enum in [Authorization]
     * @return A [String] version of a [JWTPrincipal] that the application will accept as valid.
     */
    fun makeMockToken(id: UUID, roles: List<String>): String {
        return JWT.create()
            .withAudience("account")
            .withSubject("Authentication")
            .withClaim("GroupID",id.toString())
            .withArrayClaim("roles", roles.toTypedArray())
            .withExpiresAt(Date(Long.MAX_VALUE))
            .sign(algorithm)
    }
}