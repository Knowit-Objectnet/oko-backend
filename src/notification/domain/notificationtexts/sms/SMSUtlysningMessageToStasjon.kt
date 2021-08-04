package notificationtexts.sms

import ombruk.backend.henting.domain.entity.EkstraHenting
import ombruk.backend.notification.domain.notificationtexts.Signature
import ombruk.backend.notification.domain.params.SNSInputParams
import ombruk.backend.shared.utils.formatDateRange
import ombruk.backend.utlysning.domain.entity.Utlysning
import java.time.format.DateTimeFormatter

object SMSUtlysningMessageToStasjon {
    fun getMessage(henting: EkstraHenting, utlysning: Utlysning): String {
        return "${utlysning.partnerNavn} er meldt på ekstrahenting ${henting.startTidspunkt}\n\n" +
                "Beskrivelse for ekstrahenting: ${henting.beskrivelse}\n" +
                "Kategorier: "
                Signature.signature
    }

    fun getSubject(): String = "EkstraHenting!"

    fun getInputParams(henting: EkstraHenting, utlysning: Utlysning): SNSInputParams {
        return SNSInputParams(getSubject(), getMessage(henting, utlysning))
    }
}

