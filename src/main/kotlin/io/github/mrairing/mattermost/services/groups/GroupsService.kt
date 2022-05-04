package io.github.mrairing.mattermost.services.groups

import io.github.mrairing.mattermost.api.bots.BotsClient
import io.github.mrairing.mattermost.api.bots.dto.BotCreationRequest
import io.github.mrairing.mattermost.api.teams.TeamsClient
import io.github.mrairing.mattermost.api.teams.dto.AddTeamMemberRequest
import io.github.mrairing.mattermost.api.users.UsersClient
import io.github.mrairing.mattermost.api.users.dto.UserAccessTokenDescription
import io.github.mrairing.mattermost.dao.GroupsDao
import io.github.mrairing.mattermost.endpoints.dto.WebhookCommandRequest
import io.github.mrairing.mattermost.endpoints.dto.WebhookCommandResponse
import io.github.mrairing.mattermost.jooq.tables.records.GroupsRecord
import io.github.mrairing.mattermost.properties.MattermostProperties
import io.github.mrairing.mattermost.services.groups.GroupsResponses.groupAlreadyExists
import io.github.mrairing.mattermost.services.groups.GroupsResponses.groupCreateHelp
import io.github.mrairing.mattermost.services.groups.GroupsResponses.groupCreateResponse
import io.github.mrairing.mattermost.services.groups.GroupsResponses.groupDeleteHelp
import io.github.mrairing.mattermost.services.groups.GroupsResponses.groupDeletedResponse
import io.github.mrairing.mattermost.services.groups.GroupsResponses.groupEditHelp
import io.github.mrairing.mattermost.services.groups.GroupsResponses.groupEditResponse
import io.github.mrairing.mattermost.services.groups.GroupsResponses.groupsInfoResponse
import io.github.mrairing.mattermost.services.groups.GroupsResponses.invalidGroupName
import io.github.mrairing.mattermost.services.groups.GroupsResponses.noSuchGroup
import io.github.mrairing.mattermost.services.reconciliation.GroupsReconciliationService
import jakarta.inject.Singleton

const val deletePrefix = "yes i'm sure i want to delete "
const val nameRegexStr = "[a-z_\\-0-9.]{3,22}"

@Singleton
class GroupsService(
    private val mattermostProperties: MattermostProperties,
    private val usersClient: UsersClient,
    private val botsClient: BotsClient,
    private val teamsClient: TeamsClient,
    private val groupsDao: GroupsDao,
    private val groupsReconciliationService: GroupsReconciliationService,
) {
    private val groupNameOnlyRegex = "@?(?<groupName>$nameRegexStr)".toRegex()
    private val editRegex = "@?(?<groupName>$nameRegexStr)\\s+(?<operation>add|remove) .+".toRegex()

    suspend fun getAllGroupsMMIds(): Set<String> {
        return groupsDao.findAll().map { it.mmId }.toSet()
    }

    suspend fun findByName(groupName: String): GroupsRecord? {
        return groupsDao.findByName(groupName)
    }

    suspend fun findAllUsersMMIds(group: GroupsRecord): List<String> {
        return groupsDao.findAllUsersMMIds(group.id)
    }

    suspend fun groupCreate(data: WebhookCommandRequest): WebhookCommandResponse {
        val text = data.text.trim()
        val matchResult = groupNameOnlyRegex.matchEntire(text)
        return if (matchResult != null) {
            val groupName = matchResult.groups["groupName"]!!.value
            val groupEntity = findByName(groupName)
            if (groupEntity != null) {
                groupAlreadyExists(groupName)
            } else {
                createGroup(groupName)
            }
        } else {
            if (text.isEmpty()) {
                groupCreateHelp()
            } else {
                invalidGroupName(text)
            }
        }
    }

    private suspend fun createGroup(groupName: String): WebhookCommandResponse {
        val bot = botsClient.createBot(
            BotCreationRequest(
                username = groupName,
                displayName = null,
                description = "Group $groupName"
            )
        )

        val accessToken =
            usersClient.createUserAccessToken(bot.userId, UserAccessTokenDescription("Group bot token"))

        groupsDao.save(
            GroupsRecord().apply {
                mmId = bot.userId
                token = accessToken.token
                name = groupName
            }
        )

        val teamId = mattermostProperties.teamId
        teamsClient.addTeamMember(teamId, AddTeamMemberRequest(teamId, bot.userId))

        return groupCreateResponse(groupName)
    }

    suspend fun groupDelete(data: WebhookCommandRequest): WebhookCommandResponse {
        val text = data.text.trim()

        return if (text.startsWith(deletePrefix)) {
            val textAfterPrefix = text.substringAfter(deletePrefix).trim()
            val matchResult = groupNameOnlyRegex.matchEntire(textAfterPrefix)

            if (matchResult != null) {
                val groupName = matchResult.groups["groupName"]!!.value
                val groupEntity = findByName(groupName)
                if (groupEntity != null) {
                    deleteGroup(groupEntity)
                } else {
                    noSuchGroup(groupName)
                }
            } else {
                if (textAfterPrefix.isEmpty()) {
                    groupDeleteHelp()
                } else {
                    invalidGroupName(textAfterPrefix)
                }
            }
        } else {
            groupDeleteHelp()
        }
    }

    private suspend fun deleteGroup(groupEntity: GroupsRecord): WebhookCommandResponse {
        val botUserId = groupEntity.mmId
        teamsClient.removeTeamMember(mattermostProperties.teamId, botUserId)
        botsClient.updateBot(
            botUserId,
            BotCreationRequest("deleted_${System.currentTimeMillis()}_${groupEntity.name}", null, null)
        )
        botsClient.disableBot(botUserId)
        val usersMMIds = groupsDao.findAllUsersMMIds(groupEntity.id)
        groupsDao.removeFromGroup(groupEntity.id, usersMMIds)
        groupsReconciliationService.reconcileUsers(usersMMIds)
        groupsDao.delete(groupEntity)
        return groupDeletedResponse(groupEntity.name)
    }

    suspend fun groupEdit(data: WebhookCommandRequest): WebhookCommandResponse {
        val text = data.text.trim()
        val matchResult = editRegex.matchEntire(text)
        return if (matchResult != null) {
            val groupName = matchResult.groups["groupName"]!!.value
            val operation = matchResult.groups["operation"]!!.value

            val groupEntity = findByName(groupName)
            if (groupEntity != null) {
                if (operation == "add") {
                    addToGroup(groupEntity, data)
                } else {
                    removeFromGroup(groupEntity, data)
                }
            } else {
                noSuchGroup(groupName)
            }
        } else {
            groupEditHelp()
        }
    }

    private suspend fun removeFromGroup(groupEntity: GroupsRecord, data: WebhookCommandRequest): WebhookCommandResponse {
        val userMentionsIds = data.userMentionsIds?.distinct()
        if (userMentionsIds == null || userMentionsIds.isEmpty()) {
            return groupEditHelp()
        }

        val groupsNotUsers = groupsDao.findAllByMmIds(userMentionsIds)
        val groupsIds = groupsNotUsers.map { it.mmId }.toSet()

        val usersMmIds = userMentionsIds.filter { it !in groupsIds }

        val alreadyInGroup = groupsDao.findUserMMIdsAlreadyInGroup(groupEntity.id, usersMmIds)
        val notInGroup = usersMmIds - alreadyInGroup

        groupsDao.removeFromGroup(groupEntity.id, alreadyInGroup)
        groupsReconciliationService.reconcileUsers(alreadyInGroup)

        return groupEditResponse(
            getGroupInfo(groupEntity),
            null,
            null,
            alreadyInGroup,
            notInGroup,
            data
        )
    }

    private suspend fun getGroupInfo(groupEntity: GroupsRecord): GroupsResponses.GroupInfo {
        val usersInGroupIds = groupsDao.findAllUsersMMIds(groupEntity.id)
        val users = if (usersInGroupIds.isEmpty())
            emptyList()
        else
            usersClient.getUsersByIds(usersInGroupIds)

        return GroupsResponses.GroupInfo(
            groupEntity.name,
            users,
            emptyList()
        )
    }

    private suspend fun getAllGroupsInfo(): List<GroupsResponses.GroupInfo> {
        val usersMMIdsByGroupName = groupsDao.findAllUsersMMIdsGroupedByGroupName()
        if (usersMMIdsByGroupName.isEmpty()) {
            return emptyList()
        }

        val allUsersMMIds = usersMMIdsByGroupName.values.asSequence().flatten().distinct().toList()
        val allUsers = usersClient.getUsersByIds(allUsersMMIds)
        val usersByMMId = allUsers.associateBy { it.id }

        val groupInfos = usersMMIdsByGroupName.map { (groupName, userIds) ->
            GroupsResponses.GroupInfo(
                groupName,
                userIds.mapNotNull { uMMId -> usersByMMId[uMMId] },
                emptyList()
            )
        }

        return groupInfos.sortedBy { it.groupName }
    }

    private suspend fun addToGroup(groupEntity: GroupsRecord, data: WebhookCommandRequest): WebhookCommandResponse {
        val userMentionsIds = data.userMentionsIds?.distinct()
        if (userMentionsIds == null || userMentionsIds.isEmpty()) {
            return groupEditHelp()
        }

        val groupsNotUsers = groupsDao.findAllByMmIds(userMentionsIds)
        val groupsIds = groupsNotUsers.map { it.mmId }.toSet()

        val usersMmIds = userMentionsIds.filter { it !in groupsIds }

        val alreadyInGroup = groupsDao.findUserMMIdsAlreadyInGroup(groupEntity.id, usersMmIds)
        val newToGroup = usersMmIds - alreadyInGroup

        groupsDao.addToGroup(groupEntity.id, newToGroup)
        groupsReconciliationService.reconcileUsers(newToGroup)

        return groupEditResponse(
            getGroupInfo(groupEntity),
            newToGroup,
            alreadyInGroup,
            null,
            null,
            data
        )
    }

    suspend fun groupInfo(data: WebhookCommandRequest): WebhookCommandResponse {
        val text = data.text.trim()
        val matchResult = groupNameOnlyRegex.matchEntire(text)
        return if (matchResult != null) {
            val groupName = matchResult.groups["groupName"]!!.value
            val groupEntity = findByName(groupName)
            if (groupEntity == null) {
                noSuchGroup(groupName)
            } else {
                specificGroupInfo(groupEntity)
            }
        } else {
            if (text.isEmpty()) {
                allGroupsInfo()
            } else {
                invalidGroupName(text)
            }
        }
    }

    private suspend fun specificGroupInfo(groupEntity: GroupsRecord): WebhookCommandResponse {
        return groupsInfoResponse(listOf(getGroupInfo(groupEntity)))
    }

    private suspend fun allGroupsInfo(): WebhookCommandResponse {
        return groupsInfoResponse(getAllGroupsInfo())
    }
}
