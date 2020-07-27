package ombruk.backend.shared.error

sealed class RequestError(private val msg: String ) : ServiceError(msg) {
    data class InvalidIdError(val reason: String = "") : RequestError("Invalid ID: $reason")
    data class MangledRequestBody(val reason: String = "") : RequestError("Mangled request body, $reason")
}