package ombruk.backend.notification.application.service


import com.amazonaws.services.lambda.invoke.LambdaFunction
import ombruk.backend.notification.domain.entity.SES
import ombruk.backend.notification.domain.params.SESCreateParams
import ombruk.backend.notification.domain.params.SESVerifyParams

interface ISESService {
    @LambdaFunction(functionName = "email")
    fun sendMessage(input: SESCreateParams): SES

    @LambdaFunction(functionName = "email-verification")
    fun sendMessage(input: SESVerifyParams): SES
}