package notificationtexts.email

import notificationtexts.sms.SMSUtlysningMessageToStasjon
import ombruk.backend.aktor.domain.entity.Kontakt
import ombruk.backend.henting.domain.entity.EkstraHenting
import ombruk.backend.kategori.domain.entity.EkstraHentingKategori
import ombruk.backend.notification.domain.notificationtexts.Signature
import ombruk.backend.notification.domain.params.SESInputParams
import ombruk.backend.notification.domain.params.SNSInputParams
import ombruk.backend.shared.utils.formatDateRange
import ombruk.backend.shared.utils.formatDateTime
import ombruk.backend.utlysning.domain.entity.Utlysning
import java.time.format.DateTimeFormatter

object EmailDeletedKontaktMessage {
    fun getMessage(): String {
        return "Du er nå slettet fra systemet hos Oslo Kommune sin ombruksløsning.\n" +
                "Dette gjør at du ikke lenger får varslinger fra oss."
                Signature.signature
    }

    fun getPreviewMessage(): String {
        return "Du er nå slettet fra systemet hos Oslo Kommune sin ombruksløsning."
    }

    fun getSubject(): String = "Du er nå slettet fra systemet hos Oslo Kommune sin ombruksløsning."

    fun getInputParams(): SESInputParams {
        return SESInputParams(getSubject(), getPreviewMessage(), getMessage())
    }
}

