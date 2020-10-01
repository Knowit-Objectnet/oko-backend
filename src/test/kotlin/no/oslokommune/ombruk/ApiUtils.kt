package no.oslokommune.ombruk

import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import no.oslokommune.ombruk.shared.api.JwtMockConfig

fun testGet(path: String, bearer: String? = JwtMockConfig.regEmployeeBearer, func: TestApplicationCall.() -> Unit) =
    withTestApplication({ module(true) }) {
        handleRequest(HttpMethod.Get, path) {
            bearer?.let { addHeader(HttpHeaders.Authorization, "Bearer $it") }
        }.apply(func)
    }

fun testPost(
    path: String,
    body: String,
    bearer: String? = JwtMockConfig.regEmployeeBearer,
    func: TestApplicationCall.() -> Unit
) = withTestApplication({ module(true) }) {
    handleRequest(HttpMethod.Post, path) {
        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        bearer?.let { addHeader(HttpHeaders.Authorization, "Bearer $it") }
        setBody(body)
    }.apply(func)
}

fun testPatch(
    path: String,
    body: String,
    bearer: String? = JwtMockConfig.regEmployeeBearer,
    func: TestApplicationCall.() -> Unit
) = withTestApplication({ module(true) }) {
    handleRequest(HttpMethod.Patch, path) {
        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        bearer?.let { addHeader(HttpHeaders.Authorization, "Bearer $it") }
        setBody(body)
    }.apply(func)
}

fun testDelete(
    path: String,
    bearer: String? = JwtMockConfig.regEmployeeBearer,
    func: TestApplicationCall.() -> Unit
) = withTestApplication({ module(true) }) {
    handleRequest(HttpMethod.Delete, path) {
        bearer?.let { addHeader(HttpHeaders.Authorization, "Bearer $it") }
    }.apply(func)
}