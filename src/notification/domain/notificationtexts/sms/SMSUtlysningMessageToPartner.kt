package notificationtexts.sms

import ombruk.backend.henting.domain.entity.EkstraHenting
import ombruk.backend.notification.domain.notificationtexts.Signature
import ombruk.backend.notification.domain.params.SNSInputParams
import ombruk.backend.shared.utils.formatDateRange

object SMSUtlysningMessageToPartner {
    fun getMessage(henting: EkstraHenting): String {
        return "Du kan nå melde deg på ekstrahenting på ${henting.stasjonNavn} stasjon ${formatDateRange(henting.startTidspunkt, henting.sluttTidspunkt)}." +
                " Vær kjapp! \nLink til henting og påmelding: oko.knowit.no/ekstrahenting \n" +
                Signature.signature
    }

    fun getSubject(): String = "Ny ekstrahenting!"

    fun getInputParams(henting: EkstraHenting): SNSInputParams {
        return SNSInputParams(getSubject(), getMessage(henting))
    }
}

