package io.github.mrairing.mattermost.services

import io.github.mrairing.mattermost.api.commands.dto.Command
import io.github.mrairing.mattermost.dao.CommandsRepository
import io.github.mrairing.mattermost.dao.entities.CommandEntity
import jakarta.inject.Singleton
import javax.transaction.Transactional

@Singleton
open class CommandsService(private val commandsRepository: CommandsRepository) {

    @Transactional
    open suspend fun save(command: Command) {
        val existingEntity = find(command.trigger)
        if (existingEntity != null) {
            if (existingEntity.token != command.token) {
                existingEntity.token = checkNotNull(command.token)
                commandsRepository.update(existingEntity)
            }
        } else {
            val entity = CommandEntity()
            entity.token = checkNotNull(command.token)
            entity.trigger = command.trigger
            commandsRepository.save(entity)
        }
    }

    open suspend fun find(trigger: String): CommandEntity? {
        return commandsRepository.findByTrigger(trigger)
    }
}