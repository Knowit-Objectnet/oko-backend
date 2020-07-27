package ombruk.backend.shared.error

sealed class ValidationError(private val msg: String = "Validation failed") : ServiceError(msg) {
    data class InputError(val reason: String?): ValidationError("Bad input: $reason")
    data class InvalidStateError(val reason: String?): ValidationError("Invalid state: $reason")
}