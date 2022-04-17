package io.github.mrairing.mattermost.services

import io.github.mrairing.mattermost.api.CommandsClient
import io.github.mrairing.mattermost.api.UsersClient
import io.github.mrairing.mattermost.properties.MattermostProperties
import io.github.mrairing.mattermost.properties.WebhookProperties
import io.micronaut.runtime.event.annotation.EventListener
import io.micronaut.runtime.server.event.ServerStartupEvent
import jakarta.inject.Singleton
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging.logger

@Singleton
class CreateCommandService(
    private val commandsClient: CommandsClient,
    private val usersClient: UsersClient,
    private val mattermostProperties: MattermostProperties,
    private val webhookProperties: WebhookProperties,
    private val commandsTokenVerifier: CommandsTokenVerifier,
) {
    private val log = logger {}


    @EventListener
    fun onStartup(event: ServerStartupEvent) = runBlocking {
        checkThatIAmSystemAdmin()
        createOrUpdateGroupCommand()
    }

    private suspend fun createOrUpdateGroupCommand() {
        val commandsList = listAllCommands()

        coroutineScope {
            webhookProperties.commands.forEach { c ->
                launch {
                    val command = findCommand(c.trigger, commandsList) ?: createNew(c)
                    val updated = updateCommand(command.id, c)
                    commandsTokenVerifier.rememberToken(c.trigger, checkNotNull(updated.token))
                }
            }
        }

    }

    private suspend fun updateCommand(id: String, commandProperties: WebhookProperties.GroupCommandProperties): CommandsClient.Command {
        log.info { "Updating command ${commandProperties.trigger}" }

        val updated = commandsClient.updateCommand(
            id, CommandsClient.Command(
                id = id,
                teamId = mattermostProperties.teamId,
                trigger = commandProperties.trigger,
                autoComplete = true,
                autoCompleteHint = commandProperties.autoCompleteHint,
                autoCompleteDesc = commandProperties.autoCompleteDesc,
                displayName = commandProperties.displayName,
                description = commandProperties.description,
                url = "${webhookProperties.baseUrl}/commands/${commandProperties.trigger}",
                method = CommandsClient.Method.POST,
            )
        )
        log.info { "Updated command: $updated" }
        return updated
    }

    private suspend fun createNew(command: WebhookProperties.GroupCommandProperties): CommandsClient.Command {
        log.info { "Creating ${command.trigger} command" }
        val created = commandsClient.createCommand(
            CommandsClient.CommandCreationRequest(
                teamId = mattermostProperties.teamId,
                method = CommandsClient.Method.POST,
                trigger = command.trigger,
                url = "${webhookProperties.baseUrl}/commands/${command.trigger}"
            )
        )
        log.info { "Created command: $created" }
        return created
    }

    private suspend fun listAllCommands(): List<CommandsClient.Command> {
        log.info { "Request existing commands" }
        return commandsClient.listCommands(mattermostProperties.teamId, customOnly = true)
    }

    private fun findCommand(trigger: String, commandsList: List<CommandsClient.Command>): CommandsClient.Command? {
        val existingCommand = commandsList.find { trigger == it.trigger }
        log.info {
            if (existingCommand != null)
                "Found command: $existingCommand"
            else
                "Command $trigger is not found"
        }
        return existingCommand
    }

    private suspend fun checkThatIAmSystemAdmin() {
        log.info { "Check that i'am system admin ..." }
        val me = usersClient.getMe()
        log.info { "I'am $me" }
        val iAmAdmin = (me.roles?.split(" ")
            ?.map { it.trim() }
            ?.contains("system_admin")
            ?: false)
        if (!iAmAdmin) {
            log.error { "I'am not system admin! Please, check mattermost.auth properties!" }
            throw IllegalStateException("User is not system admin!")
        }
        log.info { "Ok, i'am system admin" }
    }
}