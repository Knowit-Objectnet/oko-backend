package ombruk.backend.shared.error

sealed class ValidationError(msg: String = "Validation failed") : ServiceError(msg) {
    data class InputError(val reason: String?) : ValidationError("Bad input: $reason")
    data class Unprocessable(val reason: String) : ValidationError(reason)
}