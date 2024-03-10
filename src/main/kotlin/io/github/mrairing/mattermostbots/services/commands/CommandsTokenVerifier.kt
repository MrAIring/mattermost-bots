package io.github.mrairing.mattermostbots.services.commands

import io.github.mrairing.mattermostbots.dao.CommandsDao
import io.github.mrairing.mattermostbots.endpoints.dto.Token
import org.springframework.stereotype.Service

@Service
class CommandsTokenVerifier(private val commandsDao: CommandsDao) {

    private fun isValidToken(commandTrigger: String, token: String): Boolean {
        return token == commandsDao.find(commandTrigger)?.token
    }

    fun assertToken(commandTrigger: String, token: Token) {
        val tokenValue = token.value
        if (!isValidToken(commandTrigger, tokenValue)) {
            throw IllegalArgumentException("token: ${tokenValue.substring(0, 4)}******** is invalid for command $commandTrigger")
        }
    }
}