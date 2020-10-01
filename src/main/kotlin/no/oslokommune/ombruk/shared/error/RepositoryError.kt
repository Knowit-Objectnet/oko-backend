package no.oslokommune.ombruk.shared.error

sealed class RepositoryError(msg: String = "An internal server error occured") : ServiceError(msg) {
    data class InsertError(val reason: String?) : RepositoryError("Failed to insert, $reason")
    data class UpdateError(val reason: String?) : RepositoryError("Failed to update, $reason")
    data class DeleteError(val reason: String?) : RepositoryError("failed to delete, $reason")
    data class SelectError(val reason: String?) : RepositoryError("Failed to read, $reason")
    data class NoRowsFound(val reason: String?) : RepositoryError("No rows found with provided ID, $reason")
}