package io.github.mrairing.mattermost.services.reconciliation

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.mrairing.mattermost.api.users.UsersClient
import io.github.mrairing.mattermost.api.users.dto.User
import io.github.mrairing.mattermost.api.users.dto.UserPatchRequest
import io.github.mrairing.mattermost.dao.GroupsDao
import io.micronaut.scheduling.annotation.Scheduled
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging.logger

@Singleton
class GroupsReconciliationService(
    private val groupsDao: GroupsDao,
    private val usersClient: UsersClient,
    private val objectMapper: ObjectMapper,
) {
    private val log = logger { }

    @Scheduled(fixedRate = "1h", initialDelay = "1s")
    fun reconcile() {
        runBlocking {
            reconcileUsers()
        }
    }

    suspend fun reconcileUsers(userIds: List<String>? = null) {
        val groupNamesByUserIds = groupsDao.findAllGroupNamesGroupedByUserIds()
        val allGroupMentions = groupsDao.findAll().map { "@${it.name}" }.toSet()
        val ids = userIds ?: groupNamesByUserIds.keys

        ids.forEach { userId ->
            reconcileUsers(userId, groupNamesByUserIds, allGroupMentions)
        }
    }

    private suspend fun reconcileUsers(
        userId: String,
        groupNamesByUserIds: Map<String, List<String>>,
        allGroupMentions: Set<String>
    ) {
        val user = getUserWithNotifyProps(userId)

        val currentMentionKeys = getMentionKeys(user)
        val currentGroupMentionKeys = groupNamesByUserIds[user.id]?.map { "@$it" }?.toSet() ?: emptySet()

        if (currentMentionKeys.isNotEmpty() || currentGroupMentionKeys.isNotEmpty()) {
            val nonGroupsMentionsKeys = currentMentionKeys - allGroupMentions
            val newMentionKeys = nonGroupsMentionsKeys + currentGroupMentionKeys

            if (currentMentionKeys != newMentionKeys) {
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