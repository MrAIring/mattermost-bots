package io.github.mrairing.mattermost.services.onduty

import io.github.mrairing.mattermost.api.users.UsersClient
import io.github.mrairing.mattermost.api.users.dto.User
import io.github.mrairing.mattermost.dao.OnDutyDao
import io.github.mrairing.mattermost.endpoints.dto.WebhookCommandRequest
import io.github.mrairing.mattermost.endpoints.dto.WebhookCommandResponse
import io.github.mrairing.mattermost.jooq.tables.records.OnDutyRecord
import io.github.mrairing.mattermost.services.groups.GroupsService
import io.github.mrairing.mattermost.services.groups.nameRegexStr
import io.github.mrairing.mattermost.services.reconciliation.ReconciliationService
import io.github.mrairing.mattermost.utils.WebhookUtils.ephemeralResponse
import jakarta.inject.Singleton
import mu.KotlinLogging.logger

@Singleton
class OnDutyService(
    private val groupsService: GroupsService,
    private val onDutyDao: OnDutyDao,
    private val usersClient: UsersClient,
    private val reconciliationService: ReconciliationService,
) {
    private val log = logger { }

    private val commandRegex = "(?<status>status)|(((@$nameRegexStr\\s+)*|(?<nobody>nobody\\s+))for\\s+(?<keyword>[\\w\\p{Punct}&&[^~]]+))".toRegex()

    suspend fun onDuty(data: WebhookCommandRequest): WebhookCommandResponse {
        val text = data.text.trim()
        val matchResult = commandRegex.matchEntire(text)

        return if (matchResult != null) {
            if (matchResult.groups["status"] != null) {
                return onDutyStatus()
            }
            val keyword = matchResult.groups["keyword"]!!.value.trim()
            val userOrGroupsIds = if (matchResult.groups["nobody"] != null) {
                emptyList()
            } else {
                if (data.userMentionsIds.isNullOrEmpty())
                    listOf(data.userId)
                else
                    data.userMentionsIds
            }

            onDutyForUsers(userOrGroupsIds, keyword)
        } else {
            onDutyHelp()
        }
    }

    private suspend fun onDutyStatus(): WebhookCommandResponse {
        val userIdsByKeyword = onDutyDao.findAllUserIdsGroupedByKeywords()
        val userIds = userIdsByKeyword.values.flatten().distinct()
        val usersByIds = if (userIds.isNotEmpty()) {
            usersClient.getUsersByIds(userIds).associateBy { it.id }
        } else {
            emptyMap()
        }

        val stringBuilder = StringBuilder("All users and keywords for on-duty:\n")

        userIdsByKeyword.forEach { (keyword, users) ->
            val usernameList = users.joinToString(separator = ", ") { "@${usersByIds[it]?.username}" }
            stringBuilder.append("* $keyword: $usernameList\n")
        }

        return ephemeralResponse(stringBuilder.toString())
    }

    private suspend fun onDutyForUsers(userOrGroupsIds: List<String>, keyword: String): WebhookCommandResponse {
        val userIds = groupsService.filterNotGroups(userOrGroupsIds.distinct()).toSet()

        val currentlyOnDuty = onDutyDao.findAllByKeyword(keyword).map { it.userMmId }.toSet()
        val newForDuty = userIds - currentlyOnDuty
        val removeFromDuty = currentlyOnDuty - userIds
        val allKeywords = onDutyDao.findAllKeywords() + keyword

        onDutyDao.insertAll(newForDuty.map { OnDutyRecord(keyword, it) })
        onDutyDao.deleteAll(keyword, removeFromDuty)

        reconciliationService.reconcileUsers(
            userIds = userIds + removeFromDuty,
            keywords = allKeywords
        )

        val gotOnDuty = if (userIds.isNotEmpty())
            usersClient.getUsersByIds(userIds)
        else
            emptyList()

        val removedFromDuty = if (removeFromDuty.isNotEmpty())
            usersClient.getUsersByIds(removeFromDuty)
        else
            emptyList()


        return onDutyResponse(keyword, gotOnDuty, removedFromDuty)
    }

    private fun onDutyResponse(
        keyword: String,
        gotOnDuty: List<User>,
        removedFromDuty: List<User>
    ): WebhookCommandResponse {
        return ephemeralResponse(
            """Duty for keyword `$keyword`
                | * [${gotOnDuty.joinToString(separator = ", ") { "@${it.username}" }}] took up duty
                | * [${removedFromDuty.joinToString(separator = ", ") { "@${it.username}" }}] removed from duty
            """.trimMargin()
        )
    }

    private fun onDutyHelp(): WebhookCommandResponse {
        return ephemeralResponse(
            """Makes you or selected users to be on duty for some <keyword>. Previously selected users for this specific keyword will be released from duty
               |Examples:
               |  1. `/on-duty for hello` - you will be notified when someone mentions `hello` word
               |  2. `/on-duty @user1 for alert` - user1 will be notified when someone mentions `alert`
               |  3. `/on-duty @user1 @user2 for example` - user1 and user2 will be notified when someone mentions `example`
               |  3. `/on-duty nobody for alert` - clear on duty status for keyword `alert`
               |  3. `/on-duty status` - shows on duty status for all keywords
            """.trimMargin()
        )
    }
}