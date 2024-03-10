package io.github.mrairing.mattermostbots.services.reconciliation

import io.github.mrairing.mattermostbots.api.UsersApi
import io.github.mrairing.mattermostbots.api.dto.PatchUserRequest
import io.github.mrairing.mattermostbots.api.dto.User
import io.github.mrairing.mattermostbots.api.dto.UserNotifyProps
import io.github.mrairing.mattermostbots.dao.GroupsDao
import io.github.mrairing.mattermostbots.dao.OnDutyDao
import io.github.mrairing.mattermostbots.utils.getUsersByIds
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class ReconciliationService(
    private val groupsDao: GroupsDao,
    private val onDutyDao: OnDutyDao,
    private val usersApi: UsersApi,
) {
    private val log = logger { }

    @Scheduled(fixedRateString = "1h", initialDelayString = "1s")
    fun reconcile() {
        reconcileUsers()
        reconcileGroups()
    }

    fun reconcileGroups(groupMmIds: Collection<String>? = null) {
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
                usersApi.getUsersByIds(usersInGroupIds)

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

            usersApi.patchUser(group.mmId, PatchUserRequest().position(description))
        }
    }

    fun reconcileUsers(userIds: Collection<String>? = null, keywords: Set<String>? = null) {
        val groupNamesByUserIds = groupsDao.findAllGroupNamesGroupedByUserIds()
        val allGroupMentions = groupsDao.findAll().map { "@${it.name}" }.toSet()
        val keywordsByUserIds = onDutyDao.findAllKeywordsGroupedByUserIds()
        val allKeywords = keywords ?: onDutyDao.findAllKeywords()
        val ids = userIds ?: (groupNamesByUserIds.keys + keywordsByUserIds.keys).toSet()

        ids.forEach { userId ->
            reconcileUser(userId, groupNamesByUserIds, allGroupMentions, keywordsByUserIds, allKeywords)
        }
    }

    private fun reconcileUser(
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

    private fun reconcileMentionKeys(
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
            ?.mentionKeys
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?.toSet()
            ?: emptySet()
    }

    private fun getUserWithNotifyProps(userId: String): User {
        // WTF, can I use simple GET, please?
        return usersApi.patchUser(userId, PatchUserRequest())
    }

    private fun setNewMentionKeys(user: User, mentionKeys: String) {
        log.info { "Update mentionsKeys for user ${user.username} to $mentionKeys" }
        val newNotifyProps = (user.notifyProps ?: UserNotifyProps())
            .mentionKeys(mentionKeys)

        usersApi.patchUser(
            user.id!!,
            PatchUserRequest().notifyProps(newNotifyProps)
        )
    }
}