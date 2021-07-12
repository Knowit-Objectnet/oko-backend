package ombruk.backend.notification.application.service


import com.amazonaws.services.lambda.invoke.LambdaFunction
import ombruk.backend.notification.domain.entity.SNS
import ombruk.backend.notification.domain.entity.Verification
import ombruk.backend.notification.domain.params.SNSCreateParams
import ombruk.backend.notification.domain.params.SNSVerifyParams

interface ISNSService {
    @LambdaFunction(functionName = "sms")
    fun sendMessage(input: SNSCreateParams): SNS

    @LambdaFunction(functionName = "sms-verification")
    fun sendVerification(input: SNSVerifyParams): SNS
}