package notificationtexts.sms

import ombruk.backend.henting.domain.entity.EkstraHenting
import ombruk.backend.notification.domain.notificationtexts.Signature
import ombruk.backend.notification.domain.params.SNSInputParams
import ombruk.backend.shared.utils.formatDateRange

object SMSDeletedKontaktMessage {
    fun getMessage(): String {
        return "Du er nå slettet fra systemet hos Oslo Kommune sin ombruksløsning.\n" +
                "Dette gjør at du ikke lenger får varslinger fra oss."
                Signature.signature
    }

    fun getSubject(): String = "Du er nå slettet fra systemet hos Oslo Kommune sin ombruksløsning."

    fun getInputParams(): SNSInputParams {
        return SNSInputParams(getSubject(), getMessage())
    }
}

