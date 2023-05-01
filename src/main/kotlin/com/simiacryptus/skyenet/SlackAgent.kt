@file:Suppress("unused")

package com.simiacryptus.skyenet

import com.slack.api.Slack
import com.slack.api.methods.MethodsClient
import com.slack.api.methods.response.chat.ChatPostMessageResponse
import com.simiacryptus.skyenet.heart.GroovyInterpreter
import com.simiacryptus.skyenet.util.AbbrevBlacklistYamlDescriber
import com.simiacryptus.skyenet.body.SessionServerUtil.asJava
import com.simiacryptus.skyenet.body.SkyenetSessionServer

import java.awt.Desktop
import java.io.File
import java.net.URI

object SlackAgent {

    class SlackClients(val token: String) {

        fun methods(): MethodsClient {
            return Slack.getInstance().methods(token)
        }

        fun postMessage(channel: String, message: String): ChatPostMessageResponse {
            val methods = methods()
            return methods.chatPostMessage { it.channel(channel).text(message) }
        }

    }

    @JvmStatic
    fun main(args: Array<String>) {
        val port = 8082
        val baseURL = "http://localhost:$port"
        val server = object : SkyenetSessionServer(
            applicationName = "SlackAgent",
            yamlDescriber = AbbrevBlacklistYamlDescriber(
                "com.slack.api"
            ),
            baseURL = baseURL,
            model = "gpt-4-0314"
        ) {
            override fun hands() = mapOf(
                "slack" to SlackClients(File(File(System.getProperty("user.home")), "slack.key").readText().trim()) as Object,
            ).asJava

            override fun toString(e: Throwable): String {
                return e.message ?: e.toString()
            }

            override fun heart(hands: java.util.Map<String, Object>): Heart = GroovyInterpreter(hands)
        }.start(port)
        Desktop.getDesktop().browse(URI(baseURL))
        server.join()
    }

}
