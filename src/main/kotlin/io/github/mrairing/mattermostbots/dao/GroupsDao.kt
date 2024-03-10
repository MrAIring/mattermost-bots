package io.github.mrairing.mattermostbots.dao

import io.github.mrairing.mattermostbots.jooq.Tables.GROUPS
import io.github.mrairing.mattermostbots.jooq.Tables.GROUPS_USERS
import io.github.mrairing.mattermostbots.jooq.tables.records.GroupsRecord
import org.jooq.DSLContext
import org.springframework.stereotype.Component

@Component
class GroupsDao(private val db: DSLContext) {
     fun findByName(groupName: String): GroupsRecord? {
        return db.selectFrom(GROUPS)
            .where(GROUPS.NAME.eq(groupName))
            .fetchOne()
    }

     fun save(record: GroupsRecord) {
        db.insertInto(GROUPS)
            .set(record)
            .execute()
    }

     fun delete(groupEntity: GroupsRecord) {
        db.deleteFrom(GROUPS)
            .where(GROUPS.ID.eq(groupEntity.id))
            .execute()
    }

     fun findAllByMmIds(mmIds: Collection<String>): List<GroupsRecord> {
        return db.selectFrom(GROUPS)
            .where(GROUPS.MM_ID.`in`(mmIds))
            .fetch()
    }

     fun addToGroup(groupId: Int, usersMmIds: List<String>) {
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
            .execute()
    }

     fun removeFromGroup(groupId: Int, usersMmIds: List<String>) {
        if (usersMmIds.isEmpty()) {
            return
        }

        db.deleteFrom(GROUPS_USERS)
            .where(GROUPS_USERS.GROUP_ID.eq(groupId))
            .and(GROUPS_USERS.USER_MM_ID.`in`(usersMmIds))
            .execute()
    }

     fun findUserMMIdsAlreadyInGroup(groupId: Int, usersMmIds: List<String>): List<String> {
        if (usersMmIds.isEmpty()) {
            return emptyList()
        }
        return db.select(GROUPS_USERS.USER_MM_ID)
            .from(GROUPS_USERS)
            .where(GROUPS_USERS.USER_MM_ID.`in`(usersMmIds))
            .and(GROUPS_USERS.GROUP_ID.eq(groupId))
            .fetch(GROUPS_USERS.USER_MM_ID)
    }

     fun findAllUsersMMIds(groupId: Int): List<String> {
        return db.select(GROUPS_USERS.USER_MM_ID)
            .from(GROUPS_USERS)
            .where(GROUPS_USERS.GROUP_ID.eq(groupId))
            .fetch(GROUPS_USERS.USER_MM_ID)
    }

     fun findAllUsersMMIdsGroupedByGroupName(): Map<String, List<String>> {
        return db.select(GROUPS.NAME, GROUPS_USERS.USER_MM_ID)
            .from(GROUPS).leftJoin(GROUPS_USERS).onKey()
            .fetch()
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

     fun findAll(): List<GroupsRecord> {
        return db.selectFrom(GROUPS).fetch()
    }

     fun findAllGroupNamesGroupedByUserIds(): Map<String, List<String>> {
        return db.select(GROUPS_USERS.USER_MM_ID, GROUPS.NAME)
            .from(GROUPS).join(GROUPS_USERS).onKey()
            .fetch()
            .groupBy(
                { it.value1() },
                { it.value2() }
            )
    }
}