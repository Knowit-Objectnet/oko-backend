package no.oslokommune.ombruk.shared.database

interface IRepository {
    fun exists(id: Int): Boolean
}