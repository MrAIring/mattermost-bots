package io.github.mrairing.mattermost.dao

import io.github.mrairing.mattermost.jooq.Tables.GROUPS
import io.github.mrairing.mattermost.jooq.Tables.GROUPS_USERS
import io.github.mrairing.mattermost.jooq.tables.records.GroupsRecord
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.jooq.DSLContext
import reactor.kotlin.core.publisher.toMono

@Singleton
class GroupsDao(private val db: DSLContext) {
    suspend fun findByName(groupName: String): GroupsRecord? {
        return db.selectFrom(GROUPS)
            .where(GROUPS.NAME.eq(groupName))
            .toMono()
            .awaitSingleOrNull()
    }

    suspend fun save(record: GroupsRecord) {
        db.insertInto(GROUPS)
            .set(record)
            .awaitFirstOrNull()
    }

    suspend fun delete(groupEntity: GroupsRecord) {
        db.deleteFrom(GROUPS)
            .where(GROUPS.ID.eq(groupEntity.id))
            .awaitFirstOrNull()
    }

    suspend fun findAllByMmIds(mmIds: List<String>): List<GroupsRecord> {
        return db.selectFrom(GROUPS)
            .where(GROUPS.MM_ID.`in`(mmIds))
            .asFlow()
            .toList()
    }

    suspend fun addToGroup(groupId: Int, usersMmIds: List<String>) {
        if (usersMmIds.isEmpty()) {
            return
        }

        db.insertInto(GROUPS_USERS)
            .columns(GROUPS_USERS.GROUP_ID, GROUPS_USERS.USER_MM_ID)
            .apply {
                usersMmIds.forEach {
                    values(groupId, it)
                }
            }
            .awaitFirstOrNull()
    }

    suspend fun removeFromGroup(groupId: Int, usersMmIds: List<String>) {
        if (usersMmIds.isEmpty()) {
            return
        }

        db.deleteFrom(GROUPS_USERS)
            .where(GROUPS_USERS.GROUP_ID.eq(groupId))
            .and(GROUPS_USERS.USER_MM_ID.`in`(usersMmIds))
            .awaitFirstOrNull()
    }

    suspend fun findUserMMIdsAlreadyInGroup(groupId: Int, usersMmIds: List<String>): List<String> {
        if (usersMmIds.isEmpty()) {
            return emptyList()
        }
        return db.select(GROUPS_USERS.USER_MM_ID)
            .from(GROUPS_USERS)
            .where(GROUPS_USERS.USER_MM_ID.`in`(usersMmIds))
            .and(GROUPS_USERS.GROUP_ID.eq(groupId))
            .asFlow()
            .map { it.value1() }
            .toList()
    }

    suspend fun findAllUsersMMIds(groupId: Int): List<String> {
        return db.select(GROUPS_USERS.USER_MM_ID)
            .from(GROUPS_USERS)
            .where(GROUPS_USERS.GROUP_ID.eq(groupId))
            .asFlow()
            .map { it.value1() }
            .toList()
    }

    suspend fun findAllUsersMMIdsGroupedByGroupName(): Map<String, List<String>> {
        return db.select(GROUPS.NAME, GROUPS_USERS.USER_MM_ID)
            .from(GROUPS).leftJoin(GROUPS_USERS).onKey()
            .asFlow()
            .toList()
            .groupBy(
                { it.value1() },
                { it.value2() }
            )
            .mapValues { (_, values) ->
                if (values.size == 1 && values[0] == null) {
                    emptyList()
                } else {
                    values
                }
            }
    }

    suspend fun findAll(): List<GroupsRecord> {
        return db.selectFrom(GROUPS)
            .asFlow()
            .toList()
    }

    suspend fun findAllGroupNamesGroupedByUserIds(): Map<String, List<String>> {
        return db.select(GROUPS_USERS.USER_MM_ID, GROUPS.NAME)
            .from(GROUPS).join(GROUPS_USERS).onKey()
            .asFlow()
            .toList()
            .groupBy(
                { it.value1() },
                { it.value2() }
            )
    }
}