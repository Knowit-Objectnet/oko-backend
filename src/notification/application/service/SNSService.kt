package ombruk.backend.notification.application.service

import com.amazonaws.services.lambda.AWSLambdaClientBuilder
import com.amazonaws.services.lambda.invoke.LambdaInvokerFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import ombruk.backend.notification.domain.entity.SNS
import ombruk.backend.notification.domain.params.SNSCreateParams
import ombruk.backend.notification.domain.params.SNSInputParams
import ombruk.backend.notification.domain.params.SNSVerifyParams

class SNSService {
    fun sendMessage(inputParams: SNSInputParams, receivers: List<String>): SNS {
        if (receivers.isEmpty()) {
            return SNS(message = "No receivers", statusCode = 200)
        }

        val lambdaSNSService = LambdaInvokerFactory.builder()
            .lambdaClient(AWSLambdaClientBuilder.defaultClient())
            .build(ISNSLambdaService::class.java)

        val failed: MutableList<SNS> = ArrayList()


        //NOTE: The delay-logic is added to help mitigate messages not being received under high load. The number of
        // SMS that can be sent each second is 20, with one message possibly consisting of multiple SMSs
        // (140 ASCII characters per SMS). This delay aims to push through messages at half the speed possible.
        // That way, two of these processes should be possible to be ran at the same time, before dropping messages
        // due to frequency limits.

        //Assuming 140 characters available per message
        val smsPerMessage = Math.ceil(inputParams.message.length/140.0)
        val allowedMessagesPerSecond = 20.0/smsPerMessage

        println("Message length: ${inputParams.message.length}")
        println("smsPerMessage: ${smsPerMessage} | allowedMessagesPerSecond: ${allowedMessagesPerSecond}")
        receivers.map {
            runBlocking{
                val request = SNSCreateParams(inputParams.subject, inputParams.message, number = it)
                val response = lambdaSNSService.sendMessage(request)
                if (response.statusCode != 200) failed.add(response)
                println("Waiting ${2000/(allowedMessagesPerSecond-1)} milliseconds")
                delay((2000/(allowedMessagesPerSecond-1)).toLong())
            }
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