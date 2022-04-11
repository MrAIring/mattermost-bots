package io.github.mrairing.mattermost.service

import io.github.mrairing.mattermost.api.CommandsClient
import io.github.mrairing.mattermost.api.UsersClient
import io.micronaut.runtime.event.annotation.EventListener
import io.micronaut.runtime.server.event.ServerStartupEvent
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

@Singleton
class CreateCommandService(
    private val commandsClient: CommandsClient,
    private val usersClient: UsersClient
) {
    private val log = LoggerFactory.getLogger(CreateCommandService::class.java)

    @EventListener
    fun onStartup(event: ServerStartupEvent) = runBlocking {
        checkThatIAmSystemAdmin()


    }

    private suspend fun checkThatIAmSystemAdmin() {
        val me = usersClient.getMe()
        val iAmAdmin = (me.roles?.split(",")
            ?.map { it.trim() }
            ?.contains("system_admin")
            ?: false)
        if (!iAmAdmin) {
            throw IllegalStateException("User is not system admin!")
        }
    }
}