package notificationtexts.sms

import ombruk.backend.henting.domain.entity.HentingWrapper
import ombruk.backend.notification.domain.notificationtexts.Signature
import ombruk.backend.notification.domain.params.SNSInputParams
import ombruk.backend.shared.utils.formatDateRange

object SMSVektManglerMessage {
    fun getMessage(hentinger: List<HentingWrapper>): String {
        return "Obs! Du har ikke registrert vekt på alle hentingene dine!" +
                Signature.signature
    }

    fun getSubject(): String = "Husk å registrere vekt!"

    fun getInputParams(hentinger: List<HentingWrapper>): SNSInputParams {
        return SNSInputParams(getSubject(), getMessage(hentinger))
    }
}

