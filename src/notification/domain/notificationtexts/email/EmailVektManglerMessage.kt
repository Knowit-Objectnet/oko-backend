package notificationtexts.email

import ombruk.backend.henting.domain.entity.EkstraHenting
import ombruk.backend.henting.domain.entity.HentingWrapper
import ombruk.backend.notification.domain.notificationtexts.Signature
import ombruk.backend.notification.domain.params.SESInputParams
import ombruk.backend.notification.domain.params.SNSInputParams
import ombruk.backend.shared.utils.formatDateRange
import java.time.format.DateTimeFormatter

object EmailVektManglerMessage {
    fun getMessage(hentinger: List<HentingWrapper>): String {
        return "Du har ikke registrert vekt på følgende hentinger:\n" +
                listHentinger(hentinger) +
                "\n\nVi ønsker at du registrerer vekt på alle hentingene slik at vi lettere kan føre statistikk." +
                Signature.signature
    }

    fun getPreviewMessage(): String {
        return "Obs! Du har ikke registrert vekt på alle hentingene dine!"
    }

    fun getSubject(): String = "Husk å registrere vekt!"

    fun getInputParams(hentinger: List<HentingWrapper>): SESInputParams {
        return SESInputParams(getSubject(), getPreviewMessage(), getMessage(hentinger))
    }

    private fun listHentinger(hentinger: List<HentingWrapper>): String {
        return hentinger.fold("") { acc, wrapper ->
            acc +
                "\n\t${wrapper.stasjonNavn} stasjon - ${wrapper.startTidspunkt.format(DateTimeFormatter.ofPattern("dd.MM.yy"))}" +
                " Lenke til registrering: oko.knowit.no/vekt/${wrapper.id}"
        }
    }
}

