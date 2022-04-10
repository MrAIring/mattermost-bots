package io.github.mrairing.mattermost.service

import io.github.mrairing.mattermost.api.BotsClient
import io.github.mrairing.mattermost.api.UsersClient
import io.micronaut.runtime.event.annotation.EventListener
import io.micronaut.runtime.server.event.ServerStartupEvent
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

@Singleton
class CreateBotService(
    private val botsClient: BotsClient,
    private val usersClient: UsersClient
) {
    private val logger = LoggerFactory.getLogger(CreateBotService::class.java)

    @EventListener
    fun onStartup(event: ServerStartupEvent) = runBlocking {
        logger.info(usersClient.getMe())

        val bots = botsClient.getBots()
        logger.info("$bots")
//        val bot = botsClient.createBot(
//            BotsClient.BotCreationRequest(
//                username = "test-bot-username",
//                display_name = "Test Bot",
//                description = "Test Bot Description"
//            )
//        )
//        logger.info("$bot")
    }
}