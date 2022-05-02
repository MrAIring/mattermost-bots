package io.github.mrairing.mattermost.services.commands

import jakarta.inject.Singleton

@Singleton
class CommandsTokenVerifier(private val commandsService: CommandsService) {

    private suspend fun isValidToken(commandTrigger: String, token: String): Boolean {
        return token == commandsService.find(commandTrigger)?.token
    }

    suspend fun assertToken(commandTrigger: String, token: String) {
        if (!isValidToken(commandTrigger, token)) {
            throw IllegalArgumentException("token: ${token.substring(0, 4)}******** is invalid for command $commandTrigger")
        }
    }
}