package notificationtexts.sms

import ombruk.backend.henting.domain.entity.EkstraHenting
import ombruk.backend.notification.domain.params.SNSInputParams
import ombruk.backend.shared.utils.formatDateRange

class SMSUtlysningMessage {
    companion object {
        fun getMessage(henting: EkstraHenting): String {
            return "Du kan nå melde deg på ekstrahenting på ${henting.stasjonNavn} stasjon ${formatDateRange(henting.startTidspunkt, henting.sluttTidspunkt)}." +
                    " Vær kjapp! \nLink til henting og påmelding: oko.knowit.no/ekstrahenting \n" +
                    "\n" +
                    "Oslo kommune \n" +
                    "Renovasjonsetaten"
        }

        fun getSubject(): String = "EkstraHenting!"

        fun getInputParams(henting: EkstraHenting): SNSInputParams {
            return SNSInputParams(getSubject(), getMessage(henting))
        }
    }
}

