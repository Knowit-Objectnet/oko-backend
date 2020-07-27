package ombruk.backend.api

import ombruk.backend.service.ServiceError

sealed class RequestError(private val msg: String ) : ServiceError(msg) {
    data class InvalidIdError(val reason: String = "") : RequestError("Invalid ID: $reason")
    data class MangledRequestBody(val reason: String = "") : RequestError("Mangled request body, $reason")
}