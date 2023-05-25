package io.github.mrairing.mattermost.services.reconciliation

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.mrairing.mattermost.api.users.UsersClient
import io.github.mrairing.mattermost.api.users.dto.User
import io.github.mrairing.mattermost.api.users.dto.UserPatchRequest
import io.github.mrairing.mattermost.dao.GroupsDao
import io.github.mrairing.mattermost.dao.OnDutyDao
import io.micronaut.scheduling.annotation.Scheduled
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging.logger

@Singleton
class ReconciliationService(
    private val groupsDao: GroupsDao,
    private val usersClient: UsersClient,
    private val objectMapper: ObjectMapper,
    private val onDutyDao: OnDutyDao,
) {
    private val log = logger { }

    @Scheduled(fixedRate = "1h", initialDelay = "1s")
    fun reconcile() {
        runBlocking {
            reconcileUsers()
            reconcileGroups()
        }
    }

    suspend fun reconcileGroups(groupMmIds: Collection<String>? = null) {
        val groups = if (groupMmIds != null) {
            groupsDao.findAllByMmIds(groupMmIds)
        } else {
            groupsDao.findAll()
        }

        groups.forEach { group ->
            val usersInGroupIds = groupsDao.findAllUsersMMIds(group.id)
            val users = if (usersInGroupIds.isEmpty())
                emptyList()
            else
                usersClient.getUsersByIds(usersInGroupIds)

            val description = users
                .map { it.username }
                .sortedBy { it }
                .joinToString(" ") { "@${it}" }
                .run {
                    if (length > 128) {
                        substring(0, 124) + "..."
                    } else {
                        this
                    }
                }

            log.info { "Update '${group.name}' description to '$description'" }

            usersClient.patchUser(group.mmId, UserPatchRequest(position = description))
        }
    }

    suspend fun reconcileUsers(userIds: Collection<String>? = null, keywords: Set<String>? = null) {
        val groupNamesByUserIds = groupsDao.findAllGroupNamesGroupedByUserIds()
        val allGroupMentions = groupsDao.findAll().map { "@${it.name}" }.toSet()
        val keywordsByUserIds = onDutyDao.findAllKeywordsGroupedByUserIds()
        val allKeywords = keywords ?: onDutyDao.findAllKeywords()
        val ids = userIds ?: (groupNamesByUserIds.keys + keywordsByUserIds.keys).toSet()

        ids.forEach { userId ->
            reconcileUser(userId, groupNamesByUserIds, allGroupMentions, keywordsByUserIds, allKeywords)
        }
    }

    private suspend fun reconcileUser(
        userId: String,
        groupNamesByUserIds: Map<String, List<String>>,
        allGroupMentions: Set<String>,
        keywordsByUserIds: Map<String, List<String>>,
        allKeywords: Set<String>
    ) {
        val user = getUserWithNotifyProps(userId)

        val currentGroupMentionKeys = groupNamesByUserIds[user.id]?.map { "@$it" }?.toSet() ?: emptySet()
        val keywordMentionKeys = keywordsByUserIds[user.id] ?: emptySet()

        reconcileMentionKeys(
            user = user,
            mentionKeys = currentGroupMentionKeys + keywordMentionKeys,
            allTrackedMentions = allGroupMentions + allKeywords
        )
    }

    private suspend fun reconcileMentionKeys(
        user: User,
        mentionKeys: Set<String>,
        allTrackedMentions: Set<String>
    ) {
        val allUserMentionKeys = getMentionKeys(user)
        if (allUserMentionKeys.isNotEmpty() || mentionKeys.isNotEmpty()) {
            val userDefinedMentionsKeys = allUserMentionKeys - allTrackedMentions
            val newMentionKeys = userDefinedMentionsKeys + mentionKeys

            if (allUserMentionKeys != newMentionKeys) {
                setNewMentionKeys(user, newMentionKeys.joinToString(","))
            }
        }
    }

    private fun getMentionKeys(user: User): Set<String> {
        return user.notifyProps
            ?.path("mention_keys")
            ?.asText()
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?.toSet()
            ?: emptySet()
    }

    private suspend fun getUserWithNotifyProps(userId: String): User {
        // WTF, can I use simple GET, please?
        return usersClient.patchUser(userId, UserPatchRequest())
    }

    private suspend fun setNewMentionKeys(user: User, mentionKeys: String) {
        log.info { "Update mentionsKeys for user ${user.username} to $mentionKeys" }
        val newNotifyProps = (user.notifyProps ?: objectMapper.createObjectNode())
            .put("mention_keys", mentionKeys)

        usersClient.patchUser(
            user.id!!,
            UserPatchRequest(notifyProps = newNotifyProps)
        )
    }
}