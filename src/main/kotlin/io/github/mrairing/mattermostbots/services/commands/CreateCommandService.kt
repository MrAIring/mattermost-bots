package io.github.mrairing.mattermostbots.services.commands

import io.github.mrairing.mattermostbots.api.CommandsApi
import io.github.mrairing.mattermostbots.api.UsersApi
import io.github.mrairing.mattermostbots.api.dto.Command
import io.github.mrairing.mattermostbots.api.dto.CreateCommandRequest
import io.github.mrairing.mattermostbots.properties.MattermostProperties
import io.github.mrairing.mattermostbots.properties.WebhookProperties
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

const val PostMethod = "P"

@Service
class CreateCommandService(
    private val commandsApi: CommandsApi,
    private val usersApi: UsersApi,
    private val mattermostProperties: MattermostProperties,
    private val webhookProperties: WebhookProperties,
    private val commandsService: CommandsService,
) {
    private val log = logger {}


    @EventListener
    fun onStartup(event: ApplicationReadyEvent) {
        checkThatIAmSystemAdmin()
        createOrUpdateGroupCommand()
    }

    private fun createOrUpdateGroupCommand() {
        val commandsList = listAllCommands()

        webhookProperties.commands.forEach { c ->
            val command = findCommand(c.trigger, commandsList) ?: createNew(c)
            val updated = updateCommand(command.id, c)
            commandsService.save(updated)
        }
    }


    private fun updateCommand(
        id: String,
        commandProperties: WebhookProperties.GroupCommandProperties
    ): Command {
        log.info { "Updating command ${commandProperties.trigger}" }

        val updated = commandsApi.updateCommand(
            id,
            Command()
                .id(id)
                .teamId(mattermostProperties.teamId)
                .trigger(commandProperties.trigger)
                .autoComplete(true)
                .autoCompleteHint(commandProperties.autoCompleteHint)
                .autoCompleteDesc(commandProperties.autoCompleteDesc)
                .displayName(commandProperties.displayName)
                .description(commandProperties.description)
                .url("${webhookProperties.baseUrl}/commands/${commandProperties.trigger}")
                .method(PostMethod)
        )
        log.info { "Updated command: $updated" }
        return updated
    }

    private fun createNew(command: WebhookProperties.GroupCommandProperties): Command {
        log.info { "Creating ${command.trigger} command" }
        val created = commandsApi.createCommand(
            CreateCommandRequest()
                .teamId(mattermostProperties.teamId)
                .method(PostMethod)
                .trigger(command.trigger)
                .url("${webhookProperties.baseUrl}/commands/${command.trigger}")
        )
        log.info { "Created command: $created" }
        return created
    }

    private fun listAllCommands(): List<Command> {
        log.info { "Request existing commands" }
        val commands = commandsApi.listCommands(mattermostProperties.teamId, true)
        log.trace { "Existing commands: $commands" }
        return commands
    }

    private fun findCommand(trigger: String, commandsList: List<Command>): Command? {
        val existingCommand = commandsList.find { trigger == it.trigger }
        log.info {
            if (existingCommand != null)
                "Found command: $existingCommand"
            else
                "Command $trigger is not found"
        }
        return existingCommand
    }

    private fun checkThatIAmSystemAdmin() {
        log.info { "Check that i'am system admin ..." }
        val me = usersApi.getUser("me")
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