package io.github.mrairing.mattermost.services.commands

import io.github.mrairing.mattermost.api.commands.dto.Command
import io.github.mrairing.mattermost.jooq.Tables.COMMANDS
import io.github.mrairing.mattermost.jooq.tables.records.CommandsRecord
import jakarta.inject.Singleton
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.jooq.DSLContext
import reactor.kotlin.core.publisher.toMono

@Singleton
open class CommandsService(private val db: DSLContext) {

    open suspend fun save(command: Command) {
        val existingEntity = find(command.trigger)
        if (existingEntity != null) {
            if (existingEntity.token != command.token) {
                existingEntity.token = checkNotNull(command.token)
                update(existingEntity)
            }
        } else {
            val entity = CommandsRecord().apply {
                token = checkNotNull(command.token)
                trigger = command.trigger
            }
            insert(entity)
        }
    }

    private suspend fun insert(entity: CommandsRecord) {
        db.insertInto(COMMANDS)
            .set(entity)
            .awaitFirstOrNull()
    }

    private suspend fun update(existingEntity: CommandsRecord) {
        db.update(COMMANDS)
            .set(existingEntity)
            .awaitFirstOrNull()
    }

    open suspend fun find(trigger: String): CommandsRecord? {
        return db.selectFrom(COMMANDS)
                .where(COMMANDS.TRIGGER.eq(trigger))
                .toMono()
                .awaitSingleOrNull()
    }
}