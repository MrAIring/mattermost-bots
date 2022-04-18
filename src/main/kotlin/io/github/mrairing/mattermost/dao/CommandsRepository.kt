package io.github.mrairing.mattermost.dao

import io.github.mrairing.mattermost.dao.entities.CommandEntity
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.r2dbc.annotation.R2dbcRepository
import io.micronaut.data.repository.kotlin.CoroutineCrudRepository

@R2dbcRepository(dialect = Dialect.H2)
interface CommandsRepository : CoroutineCrudRepository<CommandEntity, Int> {
    suspend fun findByTrigger(trigger: String): CommandEntity?
}