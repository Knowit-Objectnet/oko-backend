package notificationtexts.email

import notificationtexts.sms.SMSUtlysningMessageToStasjon
import ombruk.backend.henting.domain.entity.EkstraHenting
import ombruk.backend.kategori.domain.entity.EkstraHentingKategori
import ombruk.backend.notification.domain.notificationtexts.Signature
import ombruk.backend.notification.domain.params.SESInputParams
import ombruk.backend.notification.domain.params.SNSInputParams
import ombruk.backend.shared.utils.formatDateRange
import ombruk.backend.shared.utils.formatDateTime
import ombruk.backend.utlysning.domain.entity.Utlysning
import java.time.format.DateTimeFormatter

object EmailUtlysningMessageToStasjon {
    fun getMessage(henting: EkstraHenting, utlysning: Utlysning): String {
        return "${utlysning.partnerNavn} har meldt seg på ekstrahenting på din stasjon ${formatDateTime(henting.startTidspunkt)}.\n" +
                "Dermed kan ingen andre partnere melde seg på denne ekstrahentingen.\n" +
                "${getBeskrivelse(henting.beskrivelse)}" +
                "${getKategoriString(henting.kategorier)}" +
                Signature.signature
    }

    fun getPreviewMessage(henting: EkstraHenting, utlysning: Utlysning): String {
        return "${utlysning.partnerNavn} er meldt på ekstrahenting ${formatDateTime(henting.startTidspunkt)}."
    }

    fun getSubject(): String = "Ny ekstrahenting!"

    fun getInputParams(henting: EkstraHenting, utlysning: Utlysning): SESInputParams {
        return SESInputParams(getSubject(), getPreviewMessage(henting, utlysning), getMessage(henting, utlysning))
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

