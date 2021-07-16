package ombruk.backend.notification.application.service

import com.amazonaws.services.lambda.AWSLambdaClientBuilder
import com.amazonaws.services.lambda.invoke.LambdaInvokerFactory
import ombruk.backend.notification.domain.entity.SNS
import ombruk.backend.notification.domain.params.SNSCreateParams
import ombruk.backend.notification.domain.params.SNSVerifyParams

class SNSService {
    fun sendMessage(message: String, receivers: List<String>): SNS {
        if (receivers.isEmpty()) {
            return SNS(message = "No receivers", statusCode = 200)
        }

        val lambdaSNSService = LambdaInvokerFactory.builder()
            .lambdaClient(AWSLambdaClientBuilder.defaultClient())
            .build(ISNSLambdaService::class.java)

        val failed: MutableList<SNS> = ArrayList()
        receivers.map {
            val request = SNSCreateParams(message, number = it)
            val response = lambdaSNSService.sendMessage(request)
            if (response.statusCode != 200) failed.add(response)
        }

        // @TODO Handle SMS failure when dealing with multiple contacts
        return SNS(message = "Success", statusCode = 200)
    }

    fun sendVerification(receiver: String): SNS {
        if (receiver.isEmpty()) {
            return SNS(message = "No receivers", statusCode = 200)
        }

        val lambdaSNSService = LambdaInvokerFactory.builder()
            .lambdaClient(AWSLambdaClientBuilder.defaultClient())
            .build(ISNSLambdaService::class.java)

        val request = SNSVerifyParams(receiver)
        val response = lambdaSNSService.sendVerification(request)

        return response
    }
}