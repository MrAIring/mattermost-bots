package io.github.mrairing.mattermostbots.services.commands

import io.github.mrairing.mattermostbots.api.dto.Command
import io.github.mrairing.mattermostbots.dao.CommandsDao
import io.github.mrairing.mattermostbots.jooq.tables.records.CommandsRecord
import org.springframework.stereotype.Service

@Service
class CommandsService(private val commandsDao: CommandsDao) {

    fun save(command: Command) {
        val existingEntity = commandsDao.find(command.trigger)
        if (existingEntity != null) {
            if (existingEntity.token != command.token) {
                existingEntity.token = checkNotNull(command.token)
                commandsDao.update(existingEntity)
            }
        } else {
            val entity = CommandsRecord().apply {
                token = checkNotNull(command.token)
                trigger = command.trigger
            }
            commandsDao.insert(entity)
        }
    }


}