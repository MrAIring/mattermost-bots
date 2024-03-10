package io.github.mrairing.mattermostbots.dao

import io.github.mrairing.mattermostbots.jooq.Tables
import io.github.mrairing.mattermostbots.jooq.tables.records.CommandsRecord
import org.jooq.DSLContext
import org.springframework.stereotype.Component

@Component
class CommandsDao(private val db: DSLContext) {
    fun insert(entity: CommandsRecord) {
        db.insertInto(Tables.COMMANDS)
            .set(entity)
            .execute()
    }

    fun update(existingEntity: CommandsRecord) {
        db.update(Tables.COMMANDS)
            .set(existingEntity)
            .execute()
    }

    fun find(trigger: String): CommandsRecord? {
        return db.selectFrom(Tables.COMMANDS)
            .where(Tables.COMMANDS.TRIGGER.eq(trigger))
            .fetchOne()
    }
}