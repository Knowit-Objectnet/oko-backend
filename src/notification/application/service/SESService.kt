package ombruk.backend.notification.application.service

import com.amazonaws.services.lambda.AWSLambdaClientBuilder
import com.amazonaws.services.lambda.invoke.LambdaInvokerFactory
import ombruk.backend.notification.domain.entity.SES
import ombruk.backend.notification.domain.params.SESCreateParams

class SESService {
    fun sendMessage(message: String, receivers: List<String>): SES {
        if (receivers.isEmpty()) {
            return SES(message = "No receivers", statusCode = 200)
        }

        val lambdaSESService = LambdaInvokerFactory.builder()
            .lambdaClient(AWSLambdaClientBuilder.defaultClient())
            .build(ISESService::class.java)

        val request = SESCreateParams(message, addresses = receivers)
        return lambdaSESService.sendMessage(request)
    }
}