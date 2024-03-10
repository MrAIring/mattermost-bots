package io.github.mrairing.mattermostbots.dao

import io.github.mrairing.mattermostbots.jooq.Tables.ON_DUTY
import io.github.mrairing.mattermostbots.jooq.tables.records.OnDutyRecord
import org.jooq.DSLContext
import org.springframework.stereotype.Component

@Component
class OnDutyDao(private val db: DSLContext) {
     fun findAllByKeyword(keyword: String): List<OnDutyRecord> {
        return db.selectFrom(ON_DUTY)
            .where(ON_DUTY.KEYWORD.eq(keyword))
            .fetch()
    }

     fun insertAll(records: Collection<OnDutyRecord>) {
        if (records.isEmpty()) {
            return
        }
        db.insertInto(ON_DUTY)
            .columns(ON_DUTY.KEYWORD, ON_DUTY.USER_MM_ID)
            .valuesOfRecords(records)
            .onDuplicateKeyIgnore()
            .execute()
    }

     fun deleteAll(keyword: String, userIds: Collection<String>) {
        if (userIds.isEmpty()) {
            return
        }

        db.delete(ON_DUTY)
            .where(ON_DUTY.KEYWORD.eq(keyword))
            .and(ON_DUTY.USER_MM_ID.`in`(userIds))
            .execute()
    }

     fun findAllKeywordsGroupedByUserIds(): Map<String, List<String>> {
        return db.select(ON_DUTY.USER_MM_ID, ON_DUTY.KEYWORD)
            .from(ON_DUTY)
            .fetch()
            .groupBy(
                { it.value1() },
                { it.value2() }
            )
    }

     fun findAllKeywords(): Set<String> {
        return db.selectDistinct(ON_DUTY.KEYWORD)
            .from(ON_DUTY)
            .fetchSet(ON_DUTY.KEYWORD)
    }

     fun findAllUserIdsGroupedByKeywords(): Map<String, List<String>> {
        return db.select(ON_DUTY.KEYWORD, ON_DUTY.USER_MM_ID)
            .from(ON_DUTY)
            .fetch()
            .groupBy(
                { it.value1() },
                { it.value2() }
            )
    }
}