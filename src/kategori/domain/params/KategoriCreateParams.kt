package ombruk.backend.kategori.domain.params

abstract class KategoriCreateParams {
    abstract val navn: String
    abstract val vektkategori: Boolean?
}