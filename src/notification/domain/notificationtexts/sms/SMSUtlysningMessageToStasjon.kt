package notificationtexts.sms

import notificationtexts.email.EmailUtlysningMessageToPartner
import ombruk.backend.henting.domain.entity.EkstraHenting
import ombruk.backend.kategori.domain.entity.EkstraHentingKategori
import ombruk.backend.notification.domain.notificationtexts.Signature
import ombruk.backend.notification.domain.params.SNSInputParams
import ombruk.backend.shared.utils.formatDateRange
import ombruk.backend.shared.utils.formatDateTime
import ombruk.backend.utlysning.domain.entity.Utlysning
import java.time.format.DateTimeFormatter

object SMSUtlysningMessageToStasjon {
    fun getMessage(henting: EkstraHenting, utlysning: Utlysning): String {
        return "${utlysning.partnerNavn} er meldt på ekstrahenting ${formatDateTime(henting.startTidspunkt)}\n" +
                "${getBeskrivelse(henting.beskrivelse)}" +
                "${getKategoriString(henting.kategorier)}" +
                Signature.signature
    }

    fun getSubject(): String = "EkstraHenting!"

    fun getInputParams(henting: EkstraHenting, utlysning: Utlysning): SNSInputParams {
        return SNSInputParams(getSubject(), getMessage(henting, utlysning))
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

