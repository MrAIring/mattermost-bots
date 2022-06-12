package io.github.mrairing.mattermost.dao

import io.github.mrairing.mattermost.jooq.Tables.ON_DUTY
import io.github.mrairing.mattermost.jooq.tables.records.OnDutyRecord
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.toSet
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.jooq.DSLContext

@Singleton
class OnDutyDao(private val db: DSLContext) {
    suspend fun findAllByKeyword(keyword: String): List<OnDutyRecord> {
        return db.selectFrom(ON_DUTY)
            .where(ON_DUTY.KEYWORD.eq(keyword))
            .asFlow()
            .toList()
    }

    suspend fun insertAll(records: Collection<OnDutyRecord>) {
        if (records.isEmpty()) {
            return
        }
        db.insertInto(ON_DUTY)
            .columns(ON_DUTY.KEYWORD, ON_DUTY.USER_MM_ID)
            .valuesOfRecords(records)
            .onDuplicateKeyIgnore()
            .awaitFirstOrNull()
    }

    suspend fun deleteAll(keyword: String, userIds: Collection<String>) {
        if (userIds.isEmpty()) {
            return
        }

        db.delete(ON_DUTY)
            .where(ON_DUTY.KEYWORD.eq(keyword))
            .and(ON_DUTY.USER_MM_ID.`in`(userIds))
            .awaitFirstOrNull()
    }

    suspend fun findAllKeywordsGroupedByUserIds(): Map<String, List<String>> {
        return db.select(ON_DUTY.USER_MM_ID, ON_DUTY.KEYWORD)
            .from(ON_DUTY)
            .asFlow()
            .toList()
            .groupBy(
                { it.value1() },
                { it.value2() }
            )
    }

    suspend fun findAllKeywords(): Set<String> {
        return db.selectDistinct(ON_DUTY.KEYWORD)
            .from(ON_DUTY)
            .asFlow()
            .map { it.value1() }
            .toSet()
    }

    suspend fun findAllUserIdsGroupedByKeywords(): Map<String, List<String>> {
        return db.select(ON_DUTY.KEYWORD, ON_DUTY.USER_MM_ID)
            .from(ON_DUTY)
            .asFlow()
            .toList()
            .groupBy(
                { it.value1() },
                { it.value2() }
            )
    }
}