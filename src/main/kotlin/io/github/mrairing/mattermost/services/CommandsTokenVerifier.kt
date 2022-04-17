package io.github.mrairing.mattermost.services

import jakarta.inject.Singleton
import java.util.concurrent.ConcurrentHashMap

@Singleton
class CommandsTokenVerifier {
    private final val tokens = ConcurrentHashMap<String, String>()

    suspend fun rememberToken(commandTrigger: String, token: String) {
        tokens[commandTrigger] = token
    }

    private suspend fun isValidToken(commandTrigger: String, token: String) =
        token == tokens[commandTrigger]

    suspend fun assertToken(commandTrigger: String, token: String) {
        if (!isValidToken(commandTrigger, token)) {
            throw IllegalArgumentException("token: ${token.substring(0, 4)}******** is invalid for command $commandTrigger")
        }
    }
}