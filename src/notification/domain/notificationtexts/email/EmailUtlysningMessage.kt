package notificationtexts.email

import ombruk.backend.henting.domain.entity.EkstraHenting
import ombruk.backend.notification.domain.notificationtexts.Signature
import ombruk.backend.notification.domain.params.SESInputParams
import ombruk.backend.notification.domain.params.SNSInputParams
import ombruk.backend.shared.utils.formatDateRange
import java.time.format.DateTimeFormatter

object EmailUtlysningMessage {
    fun getMessage(henting: EkstraHenting): String {
        return "Du kan nå melde deg på ekstrahenting på ${henting.stasjonNavn} stasjon ${
            formatDateRange(
                henting.startTidspunkt,
                henting.sluttTidspunkt
            )
        }. Vær kjapp! \nLink til henting og påmelding: oko.knowit.no/ekstrahenting" + Signature.signature
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
}
