package io.github.mrairing.mattermost.dao

import io.github.mrairing.mattermost.dao.entities.GroupEntity
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.r2dbc.annotation.R2dbcRepository
import io.micronaut.data.repository.kotlin.CoroutineCrudRepository

@R2dbcRepository(dialect = Dialect.H2)
interface GroupsRepository : CoroutineCrudRepository<GroupEntity, Int> {
    suspend fun findByName(name: String): GroupEntity?
}