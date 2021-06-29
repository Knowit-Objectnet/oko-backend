package ombruk.backend.notification.application.service


import com.amazonaws.services.lambda.invoke.LambdaFunction
import ombruk.backend.notification.domain.entity.SNS
import ombruk.backend.notification.domain.params.SNSCreateParams

interface ISNSService {
    @LambdaFunction(functionName = "sns-sms")
    fun sendMessage(input: SNSCreateParams): SNS
}