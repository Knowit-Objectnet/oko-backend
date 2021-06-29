package ombruk.backend.notification.application.service


import com.amazonaws.services.lambda.invoke.LambdaFunction
import ombruk.backend.notification.domain.entity.SES
import ombruk.backend.notification.domain.params.SESCreateParams

interface ISESService {
    @LambdaFunction(functionName = "sns-email")
    fun sendMessage(input: SESCreateParams): SES
}