package ombruk.backend.henting.domain.params

abstract class EkstraHentingUpdateParams : HentingUpdateParams(){
    abstract val beskrivelse: String?
}