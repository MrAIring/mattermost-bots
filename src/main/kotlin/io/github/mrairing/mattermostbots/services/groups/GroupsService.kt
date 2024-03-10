package io.github.mrairing.mattermostbots.services.groups

import io.github.mrairing.mattermostbots.api.BotsApi
import io.github.mrairing.mattermostbots.api.TeamsApi
import io.github.mrairing.mattermostbots.api.UsersApi
import io.github.mrairing.mattermostbots.api.dto.AddTeamMemberRequest
import io.github.mrairing.mattermostbots.api.dto.CreateBotRequest
import io.github.mrairing.mattermostbots.api.dto.CreateUserAccessTokenRequest
import io.github.mrairing.mattermostbots.dao.GroupsDao
import io.github.mrairing.mattermostbots.endpoints.dto.WebhookCommandRequest
import io.github.mrairing.mattermostbots.endpoints.dto.WebhookCommandResponse
import io.github.mrairing.mattermostbots.jooq.tables.records.GroupsRecord
import io.github.mrairing.mattermostbots.properties.MattermostProperties
import io.github.mrairing.mattermostbots.services.groups.GroupsResponses.groupAlreadyExists
import io.github.mrairing.mattermostbots.services.groups.GroupsResponses.groupCreateHelp
import io.github.mrairing.mattermostbots.services.groups.GroupsResponses.groupCreateResponse
import io.github.mrairing.mattermostbots.services.groups.GroupsResponses.groupDeleteHelp
import io.github.mrairing.mattermostbots.services.groups.GroupsResponses.groupDeletedResponse
import io.github.mrairing.mattermostbots.services.groups.GroupsResponses.groupEditHelp
import io.github.mrairing.mattermostbots.services.groups.GroupsResponses.groupEditResponse
import io.github.mrairing.mattermostbots.services.groups.GroupsResponses.groupsInfoResponse
import io.github.mrairing.mattermostbots.services.groups.GroupsResponses.invalidGroupName
import io.github.mrairing.mattermostbots.services.groups.GroupsResponses.noSuchGroup
import io.github.mrairing.mattermostbots.services.reconciliation.ReconciliationService
import io.github.mrairing.mattermostbots.utils.getUsersByIds
import org.springframework.stereotype.Service

const val deletePrefix = "yes i'm sure i want to delete "
const val nameRegexStr = "[a-z_\\-0-9.]{3,22}"

@Service
class GroupsService(
    private val mattermostProperties: MattermostProperties,
    private val usersApi: UsersApi,
    private val botsApi: BotsApi,
    private val teamsApi: TeamsApi,
    private val groupsDao: GroupsDao,
    private val reconciliationService: ReconciliationService,
) {
    private val groupNameOnlyRegex = "@?(?<groupName>$nameRegexStr)".toRegex()
    private val editRegex = "@?(?<groupName>$nameRegexStr)\\s+(?<operation>add|remove) .+".toRegex()

    fun getAllGroupsMMIds(): Set<String> {
        return groupsDao.findAll().map { it.mmId }.toSet()
    }

    fun findByName(groupName: String): GroupsRecord? {
        return groupsDao.findByName(groupName)
    }

    fun findAllUsersMMIds(group: GroupsRecord): List<String> {
        return groupsDao.findAllUsersMMIds(group.id)
    }

    fun groupCreate(data: WebhookCommandRequest): WebhookCommandResponse {
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

    private fun createGroup(groupName: String): WebhookCommandResponse {
        val bot = botsApi.createBot(
            CreateBotRequest()
                .username(groupName)
                .displayName(null)
                .description("Group $groupName")
        )

        val accessToken = usersApi.createUserAccessToken(
                bot.userId,
                CreateUserAccessTokenRequest().description("Group bot token")
        )

        groupsDao.save(
            GroupsRecord().apply {
                mmId = bot.userId
                token = accessToken.token
                name = groupName
            }
        )

        val teamId = mattermostProperties.teamId
        teamsApi.addTeamMember(
            teamId,
            AddTeamMemberRequest()
                .teamId(teamId)
                .userId(bot.userId)
        )

        return groupCreateResponse(groupName)
    }

    fun groupDelete(data: WebhookCommandRequest): WebhookCommandResponse {
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

    private fun deleteGroup(groupEntity: GroupsRecord): WebhookCommandResponse {
        val botUserId = groupEntity.mmId
        teamsApi.removeTeamMember(mattermostProperties.teamId, botUserId)
        botsApi.patchBot(
            botUserId,
            CreateBotRequest()
                .username("deleted_${System.currentTimeMillis()}_${groupEntity.name}")
                .displayName(null)
                .description(null)
        )
        botsApi.disableBot(botUserId)
        val usersMMIds = groupsDao.findAllUsersMMIds(groupEntity.id)
        groupsDao.removeFromGroup(groupEntity.id, usersMMIds)
        reconciliationService.reconcileUsers(usersMMIds)
        groupsDao.delete(groupEntity)
        return groupDeletedResponse(groupEntity.name)
    }

    fun groupEdit(data: WebhookCommandRequest): WebhookCommandResponse {
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

    private fun removeFromGroup(groupEntity: GroupsRecord, data: WebhookCommandRequest): WebhookCommandResponse {
        val userMentionsIds = data.userMentionsIds?.distinct()
        if (userMentionsIds == null || userMentionsIds.isEmpty()) {
            return groupEditHelp()
        }

        val usersMmIds = filterNotGroups(userMentionsIds)

        val alreadyInGroup = groupsDao.findUserMMIdsAlreadyInGroup(groupEntity.id, usersMmIds)
        val notInGroup = usersMmIds - alreadyInGroup

        groupsDao.removeFromGroup(groupEntity.id, alreadyInGroup)
        reconciliationService.reconcileUsers(alreadyInGroup)
        reconciliationService.reconcileGroups(listOf(groupEntity.mmId))

        return groupEditResponse(
            getGroupInfo(groupEntity),
            null,
            null,
            alreadyInGroup,
            notInGroup,
            data
        )
    }

    fun filterNotGroups(userMentionsIds: List<String>): List<String> {
        val groupsNotUsers = groupsDao.findAllByMmIds(userMentionsIds)
        val groupsIds = groupsNotUsers.map { it.mmId }.toSet()

        return userMentionsIds.filter { it !in groupsIds }
    }

    private fun getGroupInfo(groupEntity: GroupsRecord): GroupsResponses.GroupInfo {
        val usersInGroupIds = groupsDao.findAllUsersMMIds(groupEntity.id)
        val users = if (usersInGroupIds.isEmpty())
            emptyList()
        else
            usersApi.getUsersByIds(usersInGroupIds)

        return GroupsResponses.GroupInfo(
            groupEntity.name,
            users,
            emptyList()
        )
    }

    private fun getAllGroupsInfo(): List<GroupsResponses.GroupInfo> {
        val usersMMIdsByGroupName = groupsDao.findAllUsersMMIdsGroupedByGroupName()
        if (usersMMIdsByGroupName.isEmpty()) {
            return emptyList()
        }

        val allUsersMMIds = usersMMIdsByGroupName.values.asSequence().flatten().distinct().toList()
        val allUsers = usersApi.getUsersByIds(allUsersMMIds)
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

    private fun addToGroup(groupEntity: GroupsRecord, data: WebhookCommandRequest): WebhookCommandResponse {
        val userMentionsIds = data.userMentionsIds?.distinct()
        if (userMentionsIds == null || userMentionsIds.isEmpty()) {
            return groupEditHelp()
        }

        val usersMmIds = filterNotGroups(userMentionsIds)

        val alreadyInGroup = groupsDao.findUserMMIdsAlreadyInGroup(groupEntity.id, usersMmIds)
        val newToGroup = usersMmIds - alreadyInGroup

        groupsDao.addToGroup(groupEntity.id, newToGroup)
        reconciliationService.reconcileUsers(newToGroup)
        reconciliationService.reconcileGroups(listOf(groupEntity.mmId))

        return groupEditResponse(
            getGroupInfo(groupEntity),
            newToGroup,
            alreadyInGroup,
            null,
            null,
            data
        )
    }

    fun groupInfo(data: WebhookCommandRequest): WebhookCommandResponse {
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

    private fun specificGroupInfo(groupEntity: GroupsRecord): WebhookCommandResponse {
        return groupsInfoResponse(listOf(getGroupInfo(groupEntity)))
    }

    private fun allGroupsInfo(): WebhookCommandResponse {
        return groupsInfoResponse(getAllGroupsInfo())
    }
}
