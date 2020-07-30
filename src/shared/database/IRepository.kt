package ombruk.backend.shared.database

interface IRepository {
    fun exists(id: Int): Boolean
}