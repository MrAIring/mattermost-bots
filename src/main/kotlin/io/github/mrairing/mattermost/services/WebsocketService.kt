package io.github.mrairing.mattermost.services

import io.github.mrairing.mattermost.api.websocket.ReconnectingWebsocketClientFactory
import io.github.mrairing.mattermost.properties.MattermostProperties
import io.micronaut.runtime.event.annotation.EventListener
import io.micronaut.runtime.server.event.ServerStartupEvent
import jakarta.inject.Singleton
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging.logger

@Singleton
class WebsocketService(
    private val properties: MattermostProperties,
    private val websocketFactory: ReconnectingWebsocketClientFactory,
) {
    private val log = logger {}

    @EventListener
    fun onStartup(event: ServerStartupEvent) {
        runBlocking {
            for (i in 1..100) {
                launch {
                    val name = "group$i"
                    val client = websocketFactory.create(name, properties.auth.token)

                    client.messages.collect { msg ->
                        log.info { "$name FLOW: $msg" }
                    }
                }
            }
        }
    }
}