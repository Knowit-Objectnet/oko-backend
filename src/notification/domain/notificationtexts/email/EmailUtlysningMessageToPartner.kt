package notificationtexts.email

import ombruk.backend.henting.domain.entity.EkstraHenting
import ombruk.backend.kategori.domain.entity.EkstraHentingKategori
import ombruk.backend.kategori.domain.entity.Kategori
import ombruk.backend.notification.domain.notificationtexts.Signature
import ombruk.backend.notification.domain.params.SESInputParams
import ombruk.backend.notification.domain.params.SNSInputParams
import ombruk.backend.shared.utils.formatDateRange
import java.time.format.DateTimeFormatter

object EmailUtlysningMessageToPartner {
    fun getMessage(henting: EkstraHenting): String {
        return "Du kan nå melde deg på ekstrahenting på ${henting.stasjonNavn} stasjon ${
            formatDateRange(
                henting.startTidspunkt,
                henting.sluttTidspunkt
            )
        }. Vær kjapp! \n${getBeskrivelse(henting.beskrivelse)} ${getKategoriString(henting.kategorier)} \n\nLink til henting og påmelding: oko.knowit.no/ekstrahenting" + Signature.signature
    }

    fun getPreviewMessage(henting: EkstraHenting): String {
        return "Du kan nå melde deg på ekstrahenting på ${henting.stasjonNavn} stasjon ${
            henting.startTidspunkt.format(
                DateTimeFormatter.ofPattern("dd.MM.yy")
            )
        }."
    }

    fun getSubject(): String = "EkstraHenting!"

    fun getInputParams(henting: EkstraHenting): SESInputParams {
        return SESInputParams(getSubject(), getPreviewMessage(henting), getMessage(henting))
    }

    fun getKategoriString(kategorier: List<EkstraHentingKategori>): String? {
        val title = "\nKategorier: "
        var result = ""
        kategorier.map { it.kategori?.let { result = result + it.navn + ", "} }
        if (result.length > 2) return title + result.dropLast(2)
        else return null
    }

    fun getBeskrivelse(beskrivelse: String): String? {
        val title = "\nBeskrivelse for ekstrahenting: "
        if (beskrivelse.length > 1) return title + beskrivelse
        else return null
    }
}

