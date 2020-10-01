package no.oslokommune.ombruk.shared.database

interface IRepositoryUniqueName {
    fun exists(name: String): Boolean
}