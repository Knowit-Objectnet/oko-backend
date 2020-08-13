package ombruk.backend.shared.database

interface IRepositoryUniqueName {
    fun exists(name: String): Boolean
}